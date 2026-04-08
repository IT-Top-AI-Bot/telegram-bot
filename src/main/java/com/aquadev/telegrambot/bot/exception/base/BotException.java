package com.aquadev.telegrambot.bot.exception.base;

/**
 * Базовый класс для ожидаемых ошибок бота.
 * {@link #getMessage()} возвращает текст, который будет отправлен пользователю.
 */
public abstract class BotException extends RuntimeException {

    protected BotException(String userMessage) {
        super(userMessage);
    }
}
