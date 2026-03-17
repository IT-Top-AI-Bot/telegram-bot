package com.aquadev.ittopaitelegrambot.bot.callback.autohomework;

import com.aquadev.ittopaitelegrambot.bot.callback.CallbackHandler;
import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import com.aquadev.ittopaitelegrambot.bot.state.AutoHomeworkStateService;
import com.aquadev.ittopaitelegrambot.client.AutoHomeworkClient;
import com.aquadev.ittopaitelegrambot.client.dto.JournalSpecResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AutoHomeworkSpecToggleCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final AutoHomeworkClient client;
    private final AutoHomeworkStateService stateService;

    @Override
    public boolean supports(String callbackData) {
        return callbackData.startsWith(AutoHomeworkCallbackData.SPEC_TOGGLE);
    }

    @Override
    public void handle(Update update) {
        var callback = update.getCallbackQuery();
        String data = callback.getData();
        Message message = (Message) callback.getMessage();
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        long telegramUserId = callback.getFrom().getId();

        long specId = Long.parseLong(data.substring(AutoHomeworkCallbackData.SPEC_TOGGLE.length()));
        stateService.toggleSpec(telegramUserId, specId);

        List<JournalSpecResponse> allSpecs = client.getGroupSpecs(telegramUserId);

        sender.answerCallback(callback.getId());
        sender.editMarkup(chatId, messageId,
                AutoHomeworkKeyboardFactory.buildSpecKeyboard(allSpecs, stateService.getPendingSpecIds(telegramUserId)));
    }
}
