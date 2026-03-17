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
public class AutoHomeworkOpenSpecsCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final AutoHomeworkClient client;
    private final AutoHomeworkStateService stateService;

    @Override
    public boolean supports(String callbackData) {
        return AutoHomeworkCallbackData.OPEN_SPECS.equals(callbackData);
    }

    @Override
    public void handle(Update update) {
        var callback = update.getCallbackQuery();
        Message message = (Message) callback.getMessage();
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        long telegramUserId = callback.getFrom().getId();

        var settings = client.getSettings(telegramUserId);
        List<JournalSpecResponse> allSpecs = client.getGroupSpecs(telegramUserId);

        stateService.startSpecSelection(telegramUserId, settings.specIds());

        sender.answerCallback(callback.getId());
        sender.editHtml(chatId, messageId,
                "📚 <b>Выбор дисциплин</b>\n\nОтметьте дисциплины для авто-решения домашних заданий:",
                AutoHomeworkKeyboardFactory.buildSpecKeyboard(allSpecs, stateService.getPendingSpecIds(telegramUserId)));
    }
}
