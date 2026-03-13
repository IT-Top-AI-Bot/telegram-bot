package com.aquadev.ittopaitelegrambot.bot.handler;

import com.aquadev.ittopaitelegrambot.bot.state.RegistrationStateService;
import com.aquadev.ittopaitelegrambot.client.UserClient;
import com.aquadev.ittopaitelegrambot.client.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class RegistrationFlowHandler {

    private final TelegramClient telegramClient;
    private final UserClient userClient;
    private final RegistrationStateService stateService;

    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long telegramUserId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText().trim();

        switch (stateService.getStep(telegramUserId)) {
            case AWAITING_USERNAME -> {
                stateService.saveUsernameAndAdvance(telegramUserId, text);
                send(chatId, "Отлично! Теперь введите пароль:");
            }
            case AWAITING_PASSWORD -> {
                String username = stateService.getPendingUsername(telegramUserId);
                stateService.clear(telegramUserId);
                try {
                    UserResponse user = userClient.register(telegramUserId, username, text);
                    send(chatId, """
                            Регистрация завершена! Добро пожаловать, %s.
                            Используйте /help, чтобы увидеть доступные команды."""
                            .formatted(user.journalUsername()));
                } catch (HttpClientErrorException.Conflict e) {
                    send(chatId, """
                            Имя «%s» уже занято.
                            Введите /start и попробуйте другое имя."""
                            .formatted(username));
                } catch (HttpClientErrorException e) {
                    send(chatId, "Не удалось завершить регистрацию. Пожалуйста, введите /start и попробуйте снова.");
                }
            }
        }
    }

    private void send(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Не удалось отправить сообщение в ходе регистрации", e);
        }
    }
}
