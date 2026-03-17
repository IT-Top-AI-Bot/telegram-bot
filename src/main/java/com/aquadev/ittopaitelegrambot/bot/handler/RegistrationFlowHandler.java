package com.aquadev.ittopaitelegrambot.bot.handler;

import com.aquadev.ittopaitelegrambot.bot.exception.domain.registration.RegistrationConflictException;
import com.aquadev.ittopaitelegrambot.bot.exception.domain.registration.RegistrationFailedException;
import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import com.aquadev.ittopaitelegrambot.bot.state.RegistrationStateService;
import com.aquadev.ittopaitelegrambot.bot.state.RegistrationStep;
import com.aquadev.ittopaitelegrambot.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class RegistrationFlowHandler {

    private final TelegramMessageSender sender;
    private final UserClient userClient;
    private final RegistrationStateService stateService;

    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long telegramUserId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText().trim();

        var step = stateService.getStep(telegramUserId);

        if (step == RegistrationStep.AWAITING_USERNAME) {
            stateService.saveUsernameAndAdvance(telegramUserId, text);
            sender.send(chatId, "Отлично! Теперь введите пароль:");
        } else if (step == RegistrationStep.AWAITING_PASSWORD) {
            String username = stateService.getPendingUsername(telegramUserId);
            stateService.clear(telegramUserId);
            try {
                var user = userClient.register(telegramUserId, username, text);
                sender.send(chatId, """
                        Регистрация завершена! Добро пожаловать, %s.
                        Используйте /help, чтобы увидеть доступные команды."""
                        .formatted(user.journalUsername()));
            } catch (HttpClientErrorException.Conflict _) {
                throw new RegistrationConflictException(username);
            } catch (RestClientException _) {
                throw new RegistrationFailedException();
            }
        }
    }
}
