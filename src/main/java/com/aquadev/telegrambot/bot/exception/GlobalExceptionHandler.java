package com.aquadev.telegrambot.bot.exception;

import com.aquadev.telegrambot.bot.callback.credentials.CredentialsCallbackData;
import com.aquadev.telegrambot.bot.exception.base.BotException;
import com.aquadev.telegrambot.bot.exception.domain.UserNotRegisteredException;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String UNEXPECTED_ERROR_MESSAGE =
            "⚠️ Произошла непредвиденная ошибка. Пожалуйста, попробуйте позже.";

    private static final String CREDENTIALS_INVALID_MESSAGE =
            "🔒 Данные от журнала устарели или недействительны.\nНажмите кнопку ниже, чтобы обновить их:";

    private final TelegramMessageSender sender;

    public void handle(Update update, Throwable e) {
        switch (e) {
            case BotException botEx -> handleBotException(update, botEx);
            case TelegramSendException sendEx -> handleTelegramSendException(sendEx);
            case TelegramApiException apiEx -> handleTelegramApiException(apiEx);
            case HttpClientErrorException.Forbidden _ -> handleCredentialsInvalid(update);
            case HttpClientErrorException.NotFound _ -> handleBotException(update, new UserNotRegisteredException());
            default -> handleUnexpectedException(update, e);
        }
    }

    private void handleBotException(Update update, BotException e) {
        log.warn("Bot error [{}]: {}", e.getClass().getSimpleName(), e.getMessage());
        sendReply(update, e.getMessage());
    }

    private void handleTelegramSendException(TelegramSendException e) {
        log.error("Telegram send error: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e.getCause() != null ? e.getCause() : e);
    }

    private void handleTelegramApiException(TelegramApiException e) {
        log.error("Telegram API error: {}", e.getMessage());
    }

    public void handleCredentialsInvalid(Update update) {
        log.warn("Journal credentials invalid for user, prompting update");
        long chatId = extractChatId(update);
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("🔑 Обновить данные")
                                .callbackData(CredentialsCallbackData.UPDATE)
                                .build()
                )))
                .build();
        sender.send(chatId, CREDENTIALS_INVALID_MESSAGE, keyboard);
    }

    private void handleUnexpectedException(Update update, Throwable e) {
        log.error("Unexpected error while processing update", e);
        sendReply(update, UNEXPECTED_ERROR_MESSAGE);
    }

    private void sendReply(Update update, String text) {
        sender.send(extractChatId(update), text);
    }

    private long extractChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return update.getMessage().getChatId();
    }
}
