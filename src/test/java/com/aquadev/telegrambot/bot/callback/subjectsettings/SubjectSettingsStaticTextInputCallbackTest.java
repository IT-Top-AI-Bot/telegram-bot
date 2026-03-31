package com.aquadev.telegrambot.bot.callback.subjectsettings;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService;
import com.aquadev.telegrambot.client.AutoHomeworkClient;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubjectSettingsStaticTextInputCallbackTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    AutoHomeworkClient autoHomeworkClient;
    @Mock
    SubjectSettingsStateService stateService;

    SubjectSettingsStaticTextInputCallback callback;

    @BeforeEach
    void setUp() {
        callback = new SubjectSettingsStaticTextInputCallback(sender, autoHomeworkClient, stateService);
    }

    @Test
    void supports_returnsTrue_forSetStaticPrefix() {
        assertThat(callback.supports(SubjectSettingsCallbackData.SET_STATIC + "1")).isTrue();
    }

    @Test
    void supports_returnsFalse_forOther() {
        assertThat(callback.supports(SubjectSettingsCallbackData.OPEN)).isFalse();
    }

    @Test
    void handle_accessibleMessage_startsStateAndShowsInputText() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        Update update = mockCallbackUpdate(42L, 100L, 7, true, SubjectSettingsCallbackData.SET_STATIC + "1");

        given(autoHomeworkClient.getGroupSpecs(42L)).willReturn(specs);

        callback.handle(update);

        verify(stateService).startInput(42L, 1L, "Math", 100L, 7,
                SubjectSettingsStateService.InputType.STATIC_TEXT);
        verify(sender).answerCallback("cb-id");
        verify(sender).editHtml(eq(100L), eq(7), contains("Готовый текст"), any());
    }

    @Test
    void handle_unknownSpec_usesIdFallbackName() {
        Update update = mockCallbackUpdate(42L, 100L, 7, true, SubjectSettingsCallbackData.SET_STATIC + "99");

        given(autoHomeworkClient.getGroupSpecs(42L)).willReturn(List.of());

        callback.handle(update);

        verify(stateService).startInput(eq(42L), eq(99L), contains("99"), eq(100L), eq(7), any());
    }

    @Test
    void handle_inaccessibleMessage_answersWithError() {
        Update update = mockCallbackUpdate(42L, 0L, 0, false, SubjectSettingsCallbackData.SET_STATIC + "1");

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), contains("устарело"));
        verifyNoInteractions(autoHomeworkClient, stateService);
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
