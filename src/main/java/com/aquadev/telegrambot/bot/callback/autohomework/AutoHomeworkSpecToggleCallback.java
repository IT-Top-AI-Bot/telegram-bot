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
        if (!(callback.getMessage() instanceof Message message)) {
            log.warn("Message is inaccessible for callback {}", callback.getId());
            sender.answerCallback(callback.getId(), "Сообщение устарело или недоступно");
            return;
        }
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        long telegramUserId = callback.getFrom().getId();

        String specIdStr = data.substring(AutoHomeworkCallbackData.SPEC_TOGGLE.length());
        if (specIdStr.isEmpty() || !specIdStr.matches("\\d+")) {
            log.warn("Invalid specId format: '{}' in callback {}", specIdStr, callback.getId());
            sender.answerCallback(callback.getId(), "Неверный формат данных");
            return;
        }

        long specId;
        try {
            specId = Long.parseLong(specIdStr);
            stateService.toggleSpec(telegramUserId, specId);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse specId: '{}' in callback {}", specIdStr, callback.getId(), e);
            sender.answerCallback(callback.getId(), "Ошибка обработки данных");
            return;
        }

        List<JournalSpecResponse> allSpecs = client.getGroupSpecs(telegramUserId);

        sender.answerCallback(callback.getId());
        sender.editMarkup(chatId, messageId,
                AutoHomeworkKeyboardFactory.buildSpecKeyboard(allSpecs, stateService.getPendingSpecIds(telegramUserId)));
    }
}
