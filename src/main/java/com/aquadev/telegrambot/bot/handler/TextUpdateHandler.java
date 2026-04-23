package com.aquadev.telegrambot.bot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface TextUpdateHandler {

    boolean supports(Update update);

    void handle(Update update);
}
