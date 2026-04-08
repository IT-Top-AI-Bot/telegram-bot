package com.aquadev.telegrambot.bot.exception.domain;

import com.aquadev.telegrambot.bot.exception.base.BotException;

public class UserNotRegisteredException extends BotException {

    public UserNotRegisteredException() {
        super("Вы ещё не зарегистрированы.\nИспользуйте /start, чтобы привязать аккаунт журнала.");
    }
}
