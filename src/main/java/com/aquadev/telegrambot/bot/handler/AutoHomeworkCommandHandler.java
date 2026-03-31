package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.telegrambot.bot.callback.autohomework.AutoHomeworkSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@TelegramBotCommand(value = "/autohomework", description = "Настроить авто-домашки")
public class AutoHomeworkCommandHandler implements CommandHandler {

    private final AutoHomeworkSettingsService settingsService;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long telegramUserId = update.getMessage().getFrom().getId();
        settingsService.sendSettingsMessage(chatId, telegramUserId);
    }
}
