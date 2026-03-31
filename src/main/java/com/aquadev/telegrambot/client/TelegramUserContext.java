package com.aquadev.telegrambot.client;

public final class TelegramUserContext {

    public static final ScopedValue<Long> TG_USER_ID = ScopedValue.newInstance();

    private TelegramUserContext() {
    }

    public static Long get() {
        return TG_USER_ID.isBound() ? TG_USER_ID.get() : null;
    }
}
