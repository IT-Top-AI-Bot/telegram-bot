package com.aquadev.telegrambot.bot.callback.credentials;

import com.aquadev.telegrambot.bot.callback.CallbackHandler;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.RegistrationStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class UpdateCredentialsCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final RegistrationStateService stateService;

    @Override
    public boolean supports(String callbackData) {
        return CredentialsCallbackData.UPDATE.equals(callbackData);
    }

    @Override
    public void handle(Update update) {
        var callback = update.getCallbackQuery();
        Message message = (Message) callback.getMessage();
        long chatId = message.getChatId();
        long telegramUserId = callback.getFrom().getId();

        sender.answerCallback(callback.getId());
        stateService.startUpdate(telegramUserId);
        sender.send(chatId, "🔑 Введите логин от журнала:");
    }
}
