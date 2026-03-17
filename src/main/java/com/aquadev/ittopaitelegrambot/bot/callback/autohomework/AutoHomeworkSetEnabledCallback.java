package com.aquadev.ittopaitelegrambot.bot.callback.autohomework;

import com.aquadev.ittopaitelegrambot.bot.callback.CallbackHandler;
import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import com.aquadev.ittopaitelegrambot.client.AutoHomeworkClient;
import com.aquadev.ittopaitelegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.ittopaitelegrambot.client.dto.JournalSpecResponse;
import com.aquadev.ittopaitelegrambot.client.dto.UpdateAutoHomeworkSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AutoHomeworkSetEnabledCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final AutoHomeworkClient client;
    private final AutoHomeworkSettingsService settingsService;

    @Override
    public boolean supports(String callbackData) {
        return callbackData.startsWith(AutoHomeworkCallbackData.SET_ENABLED);
    }

    @Override
    public void handle(Update update) {
        var callback = update.getCallbackQuery();
        String data = callback.getData();
        Message message = (Message) callback.getMessage();
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        long telegramUserId = callback.getFrom().getId();

        boolean enabled = Boolean.parseBoolean(data.substring(AutoHomeworkCallbackData.SET_ENABLED.length()));

        AutoHomeworkSettingsResponse current = client.getSettings(telegramUserId);
        AutoHomeworkSettingsResponse updated = client.updateSettings(telegramUserId,
                new UpdateAutoHomeworkSettingsRequest(enabled, current.specIds()));
        List<JournalSpecResponse> allSpecs = client.getGroupSpecs(telegramUserId);

        sender.answerCallback(callback.getId(), enabled ? "✅ Авто-домашки включены" : "❌ Авто-домашки выключены");
        settingsService.editSettingsMessage(chatId, messageId, updated, allSpecs);
    }
}
