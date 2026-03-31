package com.aquadev.telegrambot.bot.callback.autohomework;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.AutoHomeworkStateService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AutoHomeworkCancelCallbackTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    AutoHomeworkStateService stateService;
    @Mock
    AutoHomeworkSettingsService settingsService;

    AutoHomeworkCancelCallback callback;

    @BeforeEach
    void setUp() {
        callback = new AutoHomeworkCancelCallback(sender, stateService, settingsService);
    }

    @Test
    void supports_returnsTrue_forCancel() {
        assertThat(callback.supports(AutoHomeworkCallbackData.CANCEL)).isTrue();
    }

    @Test
    void supports_returnsFalse_forOtherData() {
        assertThat(callback.supports(AutoHomeworkCallbackData.SPEC_SAVE)).isFalse();
    }

    @Test
    void handle_clearsStateAndRefreshesSettings() {
        Update update = mockCallbackUpdate(77L, 200L, 4);

        callback.handle(update);

        verify(stateService).clear(77L);
        verify(sender).answerCallback("cb-id");
        verify(settingsService).editSettingsMessage(eq(200L), eq(4), eq(77L));
    }

    private Update mockCallbackUpdate(long userId, long chatId, int messageId) {
        Update update = org.mockito.Mockito.mock(Update.class);
        CallbackQuery cq = org.mockito.Mockito.mock(CallbackQuery.class);
        Message message = org.mockito.Mockito.mock(Message.class);
        User from = org.mockito.Mockito.mock(User.class);

        given(update.getCallbackQuery()).willReturn(cq);
        given(cq.getId()).willReturn("cb-id");
        given(cq.getData()).willReturn(AutoHomeworkCallbackData.CANCEL);
        given(cq.getFrom()).willReturn(from);
        given(cq.getMessage()).willReturn(message);
        given(from.getId()).willReturn(userId);
        given(message.getChatId()).willReturn(chatId);
        given(message.getMessageId()).willReturn(messageId);
        return update;
    }
}
