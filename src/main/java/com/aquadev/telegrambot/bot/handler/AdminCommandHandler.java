package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.telegrambot.bot.callback.admin.AdminKeyboardFactory;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@TelegramBotCommand(value = "/admin", description = "Панель администратора", roles = {UserRole.ADMIN})
public class AdminCommandHandler implements CommandHandler {

    private final TelegramMessageSender sender;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        sender.sendHtml(chatId, AdminKeyboardFactory.buildPanelText(), AdminKeyboardFactory.buildPanelKeyboard());
    }
}
