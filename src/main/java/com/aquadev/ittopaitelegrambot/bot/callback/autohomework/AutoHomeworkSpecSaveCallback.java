package com.aquadev.ittopaitelegrambot.bot.callback.autohomework;

import com.aquadev.ittopaitelegrambot.bot.callback.CallbackHandler;
import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import com.aquadev.ittopaitelegrambot.bot.state.AutoHomeworkStateService;
import com.aquadev.ittopaitelegrambot.client.AutoHomeworkClient;
import com.aquadev.ittopaitelegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.ittopaitelegrambot.client.dto.JournalSpecResponse;
import com.aquadev.ittopaitelegrambot.client.dto.UpdateAutoHomeworkSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AutoHomeworkSpecSaveCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final AutoHomeworkClient client;
    private final AutoHomeworkStateService stateService;
    private final AutoHomeworkSettingsService settingsService;

    @Override
    public boolean supports(String callbackData) {
        return AutoHomeworkCallbackData.SPEC_SAVE.equals(callbackData);
    }

    @Override
    public void handle(Update update) {
        var callback = update.getCallbackQuery();
        Message message = (Message) callback.getMessage();
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        long telegramUserId = callback.getFrom().getId();

        Set<Long> specIds = stateService.getPendingSpecIds(telegramUserId);
        stateService.clear(telegramUserId);

        AutoHomeworkSettingsResponse current = client.getSettings(telegramUserId);
        AutoHomeworkSettingsResponse updated = client.updateSettings(telegramUserId,
                new UpdateAutoHomeworkSettingsRequest(current.enabled(), specIds));
        List<JournalSpecResponse> allSpecs = client.getGroupSpecs(telegramUserId);

        sender.answerCallback(callback.getId(), "💾 Дисциплины сохранены");
        settingsService.editSettingsMessage(chatId, messageId, updated, allSpecs);
    }
}
