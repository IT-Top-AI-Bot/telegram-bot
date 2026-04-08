package com.aquadev.telegrambot.bot.callback.admin;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

public final class AdminKeyboardFactory {

    private AdminKeyboardFactory() {
    }

    public static String buildPanelText() {
        return "<b>⚙️ Панель администратора</b>\n\nВыберите действие:";
    }

    public static InlineKeyboardMarkup buildPanelKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                        .text("🔄 Обновить конфиг из Cloud Config")
                        .callbackData(AdminCallbackData.REFRESH_CONFIG)
                        .build()))
                .build();
    }
}
