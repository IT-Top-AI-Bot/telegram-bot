package com.aquadev.ittopaitelegrambot.bot.callback.autohomework;

import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import com.aquadev.ittopaitelegrambot.client.AutoHomeworkClient;
import com.aquadev.ittopaitelegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.ittopaitelegrambot.client.dto.JournalSpecResponse;
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
        List<JournalSpecResponse> allSpecs = client.getGroupSpecs(telegramUserId);
        sender.sendHtml(chatId,
                AutoHomeworkKeyboardFactory.buildSettingsText(settings, allSpecs),
                AutoHomeworkKeyboardFactory.buildSettingsKeyboard(settings));
    }

    public void editSettingsMessage(long chatId, int messageId, long telegramUserId) {
        AutoHomeworkSettingsResponse settings = client.getSettings(telegramUserId);
        List<JournalSpecResponse> allSpecs = client.getGroupSpecs(telegramUserId);
        editSettingsMessage(chatId, messageId, settings, allSpecs);
    }

    public void editSettingsMessage(long chatId, int messageId,
                                    AutoHomeworkSettingsResponse settings,
                                    List<JournalSpecResponse> allSpecs) {
        sender.editHtml(chatId, messageId,
                AutoHomeworkKeyboardFactory.buildSettingsText(settings, allSpecs),
                AutoHomeworkKeyboardFactory.buildSettingsKeyboard(settings));
    }
}
