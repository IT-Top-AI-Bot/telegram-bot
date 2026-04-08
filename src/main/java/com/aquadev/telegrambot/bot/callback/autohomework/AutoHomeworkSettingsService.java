package com.aquadev.telegrambot.bot.callback.autohomework;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.AutoHomeworkClient;
import com.aquadev.telegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Переиспользуемая логика отображения экрана настроек авто-домашек.
 * Используется как командой /autohomework, так и коллбеками после изменений.
 */
@Service
@RequiredArgsConstructor
public class AutoHomeworkSettingsService {

    private final TelegramMessageSender sender;
    private final AutoHomeworkClient client;

    public void sendSettingsMessage(long chatId, long telegramUserId) {
        AutoHomeworkSettingsResponse settings = client.getSettings(telegramUserId);
        sender.sendHtml(chatId,
                AutoHomeworkKeyboardFactory.buildSettingsText(settings),
                AutoHomeworkKeyboardFactory.buildSettingsKeyboard(settings));
    }

    public void editSettingsMessage(long chatId, int messageId, long telegramUserId) {
        AutoHomeworkSettingsResponse settings = client.getSettings(telegramUserId);
        sender.editHtml(chatId, messageId,
                AutoHomeworkKeyboardFactory.buildSettingsText(settings),
                AutoHomeworkKeyboardFactory.buildSettingsKeyboard(settings));
    }

    public void editSettingsMessage(long chatId, int messageId,
                                    AutoHomeworkSettingsResponse settings,
                                    List<JournalSpecResponse> allSpecs) {
        sender.editHtml(chatId, messageId,
                AutoHomeworkKeyboardFactory.buildSettingsText(settings),
                AutoHomeworkKeyboardFactory.buildSettingsKeyboard(settings));
    }
}
