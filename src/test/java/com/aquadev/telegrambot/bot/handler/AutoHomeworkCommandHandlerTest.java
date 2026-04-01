package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.callback.autohomework.AutoHomeworkSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AutoHomeworkCommandHandlerTest {

    @Mock
    AutoHomeworkSettingsService settingsService;

    AutoHomeworkCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AutoHomeworkCommandHandler(settingsService);
    }

    @Test
    void handle_delegatesToSettingsService() {
        Update update = mockUpdate(500L, 77L);

        handler.handle(update);

        verify(settingsService).sendSettingsMessage(500L, 77L);
    }

    private Update mockUpdate(long chatId, long userId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User from = mock(User.class);
        given(update.getMessage()).willReturn(message);
        given(message.getChatId()).willReturn(chatId);
        given(message.getFrom()).willReturn(from);
        given(from.getId()).willReturn(userId);
        return update;
    }
}
