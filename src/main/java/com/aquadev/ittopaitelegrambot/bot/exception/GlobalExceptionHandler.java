package com.aquadev.ittopaitelegrambot.bot.exception;

import com.aquadev.ittopaitelegrambot.bot.exception.base.BotException;
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

    private static final String UNEXPECTED_ERROR_MESSAGE =
            "Произошла непредвиденная ошибка. Пожалуйста, попробуйте позже.";

    private final TelegramClient telegramClient;

    public void handle(Update update, Throwable e) {
        switch (e) {
            case BotException botEx -> handleBotException(update, botEx);
            case TelegramSendException sendEx -> handleTelegramSendException(sendEx);
            case TelegramApiException apiEx -> handleTelegramApiException(apiEx);
            default -> handleUnexpectedException(update, e);
        }
    }

    private void handleBotException(Update update, BotException e) {
        log.warn("Bot error [{}]: {}", e.getClass().getSimpleName(), e.getMessage());
        sendReply(update, e.getMessage());
    }

    private void handleTelegramSendException(TelegramSendException e) {
        log.error("Telegram send error: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
    }

    private void handleTelegramApiException(TelegramApiException e) {
        log.error("Telegram API error: {}", e.getMessage());
    }

    private void handleUnexpectedException(Update update, Throwable e) {
        log.error("Unexpected error while processing update", e);
        sendReply(update, UNEXPECTED_ERROR_MESSAGE);
    }

    private void sendReply(Update update, String text) {
        long chatId = update.getMessage().getChatId();
        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
        } catch (TelegramApiException ex) {
            log.error("Failed to send error reply to chat {}: {}", chatId, ex.getMessage());
        }
    }
}
