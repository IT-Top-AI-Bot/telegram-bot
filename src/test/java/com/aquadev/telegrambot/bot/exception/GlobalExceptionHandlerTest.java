package com.aquadev.telegrambot.bot.exception;

import com.aquadev.telegrambot.bot.exception.base.BotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    TelegramClient telegramClient;

    GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(telegramClient);
    }

    @Test
    void handle_botException_sendsUserMessage() throws TelegramApiException {
        Update update = mockUpdate(500L);
        BotException ex = new BotException("User-facing error") {
        };

        handler.handle(update, ex);

        ArgumentCaptor<SendMessage> cap = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(cap.capture());
        assertThat(cap.getValue().getText()).isEqualTo("User-facing error");
        assertThat(cap.getValue().getChatId()).isEqualTo("500");
    }

    @Test
    void handle_telegramSendException_doesNotSendMessage() {
        TelegramSendException ex = new TelegramSendException("send fail", new TelegramApiException("api"));

        handler.handle(null, ex);

        verifyNoInteractions(telegramClient);
    }

    @Test
    void handle_telegramApiException_doesNotSendMessage() {
        TelegramApiException ex = new TelegramApiException("api error");

        handler.handle(null, ex);

        verifyNoInteractions(telegramClient);
    }

    @Test
    void handle_unexpectedException_sendsGenericMessage() throws TelegramApiException {
        Update update = mockUpdate(500L);
        RuntimeException ex = new RuntimeException("unexpected");

        handler.handle(update, ex);

        ArgumentCaptor<SendMessage> cap = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(cap.capture());
        assertThat(cap.getValue().getText()).contains("непредвиденная ошибка");
    }

    @Test
    void handle_botException_telegramSendFails_doesNotThrow() throws TelegramApiException {
        Update update = mockUpdate(500L);
        BotException ex = new BotException("msg") {
        };
        given(telegramClient.execute(any(SendMessage.class))).willThrow(new TelegramApiException("fail"));

        // Should not throw - error is logged silently
        assertDoesNotThrow(() -> handler.handle(update, ex));
        verify(telegramClient).execute(any(SendMessage.class));
    }

    private Update mockUpdate(long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        given(update.getMessage()).willReturn(message);
        given(message.getChatId()).willReturn(chatId);
        return update;
    }
}
