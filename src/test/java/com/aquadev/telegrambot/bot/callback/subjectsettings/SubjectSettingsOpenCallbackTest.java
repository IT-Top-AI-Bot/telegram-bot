package com.aquadev.telegrambot.bot.callback.subjectsettings;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.AutoHomeworkClient;
import com.aquadev.telegrambot.client.ExecutorClient;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
import com.aquadev.telegrambot.client.dto.SubjectPromptDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubjectSettingsOpenCallbackTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    AutoHomeworkClient autoHomeworkClient;
    @Mock
    ExecutorClient executorClient;

    SubjectSettingsOpenCallback callback;

    @BeforeEach
    void setUp() {
        callback = new SubjectSettingsOpenCallback(sender, autoHomeworkClient, executorClient);
    }

    @Test
    void supports_returnsTrue_forOpen() {
        assertThat(callback.supports(SubjectSettingsCallbackData.OPEN)).isTrue();
    }

    @Test
    void supports_returnsFalse_forOther() {
        assertThat(callback.supports(SubjectSettingsCallbackData.SPEC + "1")).isFalse();
    }

    @Test
    void handle_accessibleMessage_fetchesDataAndEdits() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        var prompts = List.of(new SubjectPromptDto(1L, "Math", null, null, null));
        Update update = mockCallbackUpdate(42L, 100L, 7, true);

        given(autoHomeworkClient.getGroupSpecs(42L)).willReturn(specs);
        given(executorClient.getAllPrompts(42L)).willReturn(prompts);

        callback.handle(update);

        verify(sender).answerCallback("cb-id");
        verify(sender).editHtml(eq(100L), eq(7), any(), any());
    }

    @Test
    void handle_inaccessibleMessage_answersWithErrorAndReturns() {
        Update update = mockCallbackUpdate(42L, 0L, 0, false);

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), contains("устарело"));
        verifyNoInteractions(autoHomeworkClient, executorClient);
    }

    private Update mockCallbackUpdate(long userId, long chatId, int messageId, boolean accessible) {
        Update update = mock(Update.class);
        CallbackQuery cq = mock(CallbackQuery.class);
        User from = mock(User.class);

        given(update.getCallbackQuery()).willReturn(cq);
        given(cq.getId()).willReturn("cb-id");
        given(cq.getData()).willReturn(SubjectSettingsCallbackData.OPEN);
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

