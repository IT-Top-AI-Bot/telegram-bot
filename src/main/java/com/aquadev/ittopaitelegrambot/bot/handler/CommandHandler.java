package com.aquadev.ittopaitelegrambot.bot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandHandler {
    void handle(Update update);
}
