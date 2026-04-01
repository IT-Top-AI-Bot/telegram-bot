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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoHomeworkSpecToggleAllCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final AutoHomeworkClient client;
    private final AutoHomeworkStateService stateService;

    @Override
    public boolean supports(String callbackData) {
        return AutoHomeworkCallbackData.SPEC_TOGGLE_ALL.equals(callbackData);
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

        List<JournalSpecResponse> allSpecs = client.getGroupSpecs(telegramUserId);
        Set<Long> allSpecIds = allSpecs.stream().map(JournalSpecResponse::id).collect(Collectors.toSet());
        Set<Long> pendingIds = stateService.getPendingSpecIds(telegramUserId);

        if (pendingIds.containsAll(allSpecIds)) {
            stateService.setSpecs(telegramUserId, Set.of());
        } else {
            stateService.setSpecs(telegramUserId, allSpecIds);
        }

        sender.answerCallback(callback.getId());
        sender.editMarkup(chatId, messageId,
                AutoHomeworkKeyboardFactory.buildSpecKeyboard(allSpecs, stateService.getPendingSpecIds(telegramUserId)));
    }
}
