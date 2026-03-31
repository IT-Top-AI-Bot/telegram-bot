package com.aquadev.telegrambot.bot.callback.subjectsettings;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.AutoHomeworkClient;
import com.aquadev.telegrambot.client.ExecutorClient;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
import com.aquadev.telegrambot.client.dto.SubjectPromptDto;
import com.aquadev.telegrambot.client.dto.UpsertSubjectPromptRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubjectSettingsDeletePromptCallbackTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    AutoHomeworkClient autoHomeworkClient;
    @Mock
    ExecutorClient executorClient;

    SubjectSettingsDeletePromptCallback callback;

    @BeforeEach
    void setUp() {
        callback = new SubjectSettingsDeletePromptCallback(sender, autoHomeworkClient, executorClient);
    }

    @Test
    void supports_returnsTrue_forDelPromptPrefix() {
        assertThat(callback.supports(SubjectSettingsCallbackData.DEL_PROMPT + "1")).isTrue();
    }

    @Test
    void supports_returnsFalse_forOther() {
        assertThat(callback.supports(SubjectSettingsCallbackData.OPEN)).isFalse();
    }

    @Test
    void handle_inaccessibleMessage_answersWithError() {
        Update update = mockCallbackUpdate(42L, 0L, 0, false, SubjectSettingsCallbackData.DEL_PROMPT + "1");

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), contains("устарело"));
        verifyNoInteractions(autoHomeworkClient, executorClient);
    }

    @Test
    void handle_noExistingPrompt_answersDeletedAndEdits() {
        Update update = mockCallbackUpdate(42L, 100L, 7, true, SubjectSettingsCallbackData.DEL_PROMPT + "1");

        given(autoHomeworkClient.getGroupSpecs(42L)).willReturn(List.of(new JournalSpecResponse(1L, "Math", "MTH")));
        given(executorClient.getPromptBySpecId(42L, 1L)).willReturn(Optional.empty());

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), contains("удалён"));
        verify(sender).editHtml(eq(100L), eq(7), any(), any());
    }

    @Test
    void handle_withStaticText_upsertsWithNullPrompt() {
        var existing = new SubjectPromptDto(1L, "Math", "old prompt", null, "static text");
        Update update = mockCallbackUpdate(42L, 100L, 7, true, SubjectSettingsCallbackData.DEL_PROMPT + "1");

        given(autoHomeworkClient.getGroupSpecs(42L)).willReturn(List.of(new JournalSpecResponse(1L, "Math", "MTH")));
        given(executorClient.getPromptBySpecId(42L, 1L)).willReturn(Optional.of(existing));
        given(executorClient.upsertPrompt(eq(42L), eq(1L), any())).willReturn(
                new SubjectPromptDto(1L, "Math", null, null, "static text"));

        callback.handle(update);

        ArgumentCaptor<UpsertSubjectPromptRequest> captor = ArgumentCaptor.forClass(UpsertSubjectPromptRequest.class);
        verify(executorClient).upsertPrompt(eq(42L), eq(1L), captor.capture());
        assertThat(captor.getValue().systemPrompt()).isNull();
        assertThat(captor.getValue().staticText()).isEqualTo("static text");
    }

    @Test
    void handle_withPromptOnly_deletesRecord() {
        var existing = new SubjectPromptDto(1L, "Math", "old prompt", null, null);
        Update update = mockCallbackUpdate(42L, 100L, 7, true, SubjectSettingsCallbackData.DEL_PROMPT + "1");

        given(autoHomeworkClient.getGroupSpecs(42L)).willReturn(List.of(new JournalSpecResponse(1L, "Math", "MTH")));
        given(executorClient.getPromptBySpecId(42L, 1L)).willReturn(Optional.of(existing));

        callback.handle(update);

        verify(executorClient).deletePrompt(42L, 1L);
    }

    private Update mockCallbackUpdate(long userId, long chatId, int messageId, boolean accessible, String data) {
        Update update = mock(Update.class);
        CallbackQuery cq = mock(CallbackQuery.class);
        User from = mock(User.class);

        given(update.getCallbackQuery()).willReturn(cq);
        given(cq.getId()).willReturn("cb-id");
        given(cq.getData()).willReturn(data);
        given(cq.getFrom()).willReturn(from);
        given(from.getId()).willReturn(userId);

        if (accessible) {
            Message message = mock(Message.class);
            given(cq.getMessage()).willReturn(message);
            given(message.getChatId()).willReturn(chatId);
            given(message.getMessageId()).willReturn(messageId);
        } else {
            given(cq.getMessage()).willReturn(null);
        }
        return update;
    }
}
