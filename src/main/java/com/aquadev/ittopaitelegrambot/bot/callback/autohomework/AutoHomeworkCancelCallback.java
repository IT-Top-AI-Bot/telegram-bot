package com.aquadev.ittopaitelegrambot.bot.callback.autohomework;

import com.aquadev.ittopaitelegrambot.bot.callback.CallbackHandler;
import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import com.aquadev.ittopaitelegrambot.bot.state.AutoHomeworkStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class AutoHomeworkCancelCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final AutoHomeworkStateService stateService;
    private final AutoHomeworkSettingsService settingsService;

    @Override
    public boolean supports(String callbackData) {
        return AutoHomeworkCallbackData.CANCEL.equals(callbackData);
    }

    @Override
    public void handle(Update update) {
        var callback = update.getCallbackQuery();
        Message message = (Message) callback.getMessage();
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        long telegramUserId = callback.getFrom().getId();

        stateService.clear(telegramUserId);

        sender.answerCallback(callback.getId());
        settingsService.editSettingsMessage(chatId, messageId, telegramUserId);
    }
}
