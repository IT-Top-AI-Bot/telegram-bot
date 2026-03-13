package com.aquadev.ittopaitelegrambot.bot.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final TelegramClient telegramClient;

    public void handle(Update update, Exception e) {
        if (e instanceof TelegramApiException) {
            handleTelegramApiException(update, (TelegramApiException) e);
        } else {
            handleGenericException(update, e);
        }
    }

    private void handleTelegramApiException(Update update, TelegramApiException e) {
        log.error("Telegram API error while processing update: {}", e.getMessage());
    }

    private void handleGenericException(Update update, Exception e) {
        long chatId = update.getMessage().getChatId();
        sendErrorReply(chatId);
    }

    private void sendErrorReply(long chatId) {
        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text("Произошла непредвиденная ошибка. Пожалуйста, попробуйте позже.")
                    .build());
        } catch (TelegramApiException ex) {
            log.error("Failed to send error reply to chat {}: {}", chatId, ex.getMessage());
        }
    }
}
