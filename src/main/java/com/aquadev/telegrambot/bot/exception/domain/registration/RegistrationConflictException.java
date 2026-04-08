package com.aquadev.telegrambot.bot.exception.domain.registration;

import com.aquadev.telegrambot.bot.exception.base.BotException;

public class RegistrationConflictException extends BotException {

    public RegistrationConflictException(String username) {
        super("Имя " + username + " уже занято.\nВведите /start и попробуйте другое имя.");
    }
}
