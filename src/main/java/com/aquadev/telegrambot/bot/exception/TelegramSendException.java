package com.aquadev.telegrambot.bot.exception;

import lombok.Getter;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Getter
public class TelegramSendException extends RuntimeException {

    private final TelegramApiException telegramCause;

    public TelegramSendException(String message, TelegramApiException cause) {
        super(message, cause);
        this.telegramCause = cause;
    }
}
