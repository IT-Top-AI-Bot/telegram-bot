package com.aquadev.telegrambot.bot.exception.domain.registration;

import com.aquadev.telegrambot.bot.exception.base.BotException;

public class RegistrationConflictException extends BotException {

    public RegistrationConflictException(String username) {
        super("❌ Аккаунт журнала «" + username + "» уже привязан к другому пользователю.\nВведите /start и попробуйте с другим аккаунтом.");
    }
}
