package com.aquadev.telegrambot.bot.callback.autohomework;

import com.aquadev.telegrambot.bot.callback.CallbackHandler;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.AutoHomeworkStateService;
import com.aquadev.telegrambot.client.AutoHomeworkClient;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Slf4j
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
        if (!(callback.getMessage() instanceof Message message)) {
            log.warn("Message is inaccessible for callback {}", callback.getId());
            sender.answerCallback(callback.getId(), "Сообщение устарело или недоступно");
            return;
        }
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        long telegramUserId = callback.getFrom().getId();

        var settings = client.getSettings(telegramUserId);
        List<JournalSpecResponse> allSpecs = client.getGroupSpecs(telegramUserId);

        stateService.startSpecSelection(telegramUserId, settings.specIds());

        sender.answerCallback(callback.getId());
        sender.editHtml(chatId, messageId,
                "📚 <b>Предметы для авто-решения</b>\n\n"
                        + "Отметьте предметы, по которым бот будет автоматически решать ДЗ.\n\n"
                        + "<i>Тип ответа (AI или готовый текст) настраивается через «Настройка ответов по предметам».</i>",
                AutoHomeworkKeyboardFactory.buildSpecKeyboard(allSpecs, stateService.getPendingSpecIds(telegramUserId)));
    }
}
