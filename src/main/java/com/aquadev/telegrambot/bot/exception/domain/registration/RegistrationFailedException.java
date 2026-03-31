package com.aquadev.telegrambot.bot.exception.domain.registration;

import com.aquadev.telegrambot.bot.exception.base.BotException;

public class RegistrationFailedException extends BotException {

    public RegistrationFailedException() {
        super("Не удалось завершить регистрацию. Пожалуйста, введите /start и попробуйте снова.");
    }

    public RegistrationFailedException(String message) {
        super(message);
    }
}
