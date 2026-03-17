package com.aquadev.ittopaitelegrambot.bot.callback;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CallbackHandler {

    boolean supports(String callbackData);

    void handle(Update update);
}
