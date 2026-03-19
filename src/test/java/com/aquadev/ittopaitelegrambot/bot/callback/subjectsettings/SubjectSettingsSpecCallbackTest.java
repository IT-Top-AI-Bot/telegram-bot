package com.aquadev.ittopaitelegrambot.bot.callback.subjectsettings;

import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import com.aquadev.ittopaitelegrambot.client.AutoHomeworkClient;
import com.aquadev.ittopaitelegrambot.client.ExecutorClient;
import com.aquadev.ittopaitelegrambot.client.dto.JournalSpecResponse;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubjectSettingsSpecCallbackTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    AutoHomeworkClient autoHomeworkClient;
    @Mock
    ExecutorClient executorClient;

    SubjectSettingsSpecCallback callback;

    @BeforeEach
    void setUp() {
        callback = new SubjectSettingsSpecCallback(sender, autoHomeworkClient, executorClient);
    }

    @Test
    void supports_returnsTrue_forSpecPrefix() {
        assertThat(callback.supports(SubjectSettingsCallbackData.SPEC + "42")).isTrue();
    }

    @Test
    void supports_returnsFalse_forOther() {
        assertThat(callback.supports(SubjectSettingsCallbackData.OPEN)).isFalse();
    }

    @Test
    void handle_knownSpec_showsSpecDetailWithName() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        Update update = mockCallbackUpdate(42L, 100L, 7, true, SubjectSettingsCallbackData.SPEC + "1");

        given(autoHomeworkClient.getGroupSpecs(42L)).willReturn(specs);
        given(executorClient.getPromptBySpecId(42L, 1L)).willReturn(Optional.empty());

        callback.handle(update);

        verify(sender).answerCallback("cb-id");
        verify(sender).editHtml(eq(100L), eq(7), any(), any());
    }

    @Test
    void handle_unknownSpec_fallsBackToIdLabel() {
        Update update = mockCallbackUpdate(42L, 100L, 7, true, SubjectSettingsCallbackData.SPEC + "99");

        given(autoHomeworkClient.getGroupSpecs(42L)).willReturn(List.of());
        given(executorClient.getPromptBySpecId(42L, 99L)).willReturn(Optional.empty());

        callback.handle(update);

        verify(sender).editHtml(eq(100L), eq(7), contains("99"), any());
    }

    @Test
    void handle_inaccessibleMessage_answersWithError() {
        Update update = mockCallbackUpdate(42L, 0L, 0, false, SubjectSettingsCallbackData.SPEC + "1");

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), contains("устарело"));
        verifyNoInteractions(autoHomeworkClient, executorClient);
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
