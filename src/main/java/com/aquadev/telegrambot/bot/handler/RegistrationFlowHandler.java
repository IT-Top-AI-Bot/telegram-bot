package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.exception.domain.registration.RegistrationConflictException;
import com.aquadev.telegrambot.bot.exception.domain.registration.RegistrationFailedException;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.RegistrationStateService;
import com.aquadev.telegrambot.bot.state.RegistrationStep;
import com.aquadev.telegrambot.client.UserClient;
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
            sender.send(chatId, "Отлично! Теперь введите пароль от журнала:");
        } else if (step == RegistrationStep.AWAITING_PASSWORD) {
            int passwordMessageId = update.getMessage().getMessageId();
            String username = stateService.getPendingUsername(telegramUserId);
            stateService.clear(telegramUserId);
            sender.deleteMessage(chatId, passwordMessageId);
            try {
                var user = userClient.register(telegramUserId, username, text);
                sender.send(chatId, """
                        Регистрация завершена! Добро пожаловать, %s.
                        Используйте /help, чтобы увидеть доступные команды."""
                        .formatted(user.journalUsername()));
            } catch (HttpClientErrorException.Conflict _) {
                throw new RegistrationConflictException(username);
            } catch (HttpClientErrorException.Unauthorized _) {
                throw new RegistrationFailedException("Неверный логин или пароль от журнала.\nПроверьте данные и введите /start ещё раз.");
            } catch (RestClientException _) {
                throw new RegistrationFailedException();
            }
        }
    }
}
