package com.aquadev.ittopaitelegrambot.bot.exception.domain.registration;

import com.aquadev.ittopaitelegrambot.bot.exception.base.BotException;

public class RegistrationFailedException extends BotException {

    public RegistrationFailedException() {
        super("Не удалось завершить регистрацию. Пожалуйста, введите /start и попробуйте снова.");
    }
}
