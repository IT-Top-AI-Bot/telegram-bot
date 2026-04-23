package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.exception.domain.registration.RegistrationConflictException;
import com.aquadev.telegrambot.bot.exception.domain.registration.RegistrationFailedException;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.RegistrationStateService;
import com.aquadev.telegrambot.bot.state.RegistrationStep;
import com.aquadev.telegrambot.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.telegram.telegrambots.meta.api.objects.Update;

@Order(1)
@Component
@RequiredArgsConstructor
public class RegistrationFlowHandler implements TextUpdateHandler {

    private final TelegramMessageSender sender;
    private final UserClient userClient;
    private final RegistrationStateService stateService;

    @Override
    public boolean supports(Update update) {
        return update.hasMessage()
                && update.getMessage().hasText()
                && stateService.isInProgress(update.getMessage().getFrom().getId());
    }

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long telegramUserId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText().trim();

        var step = stateService.getStep(telegramUserId);

        if (step == RegistrationStep.AWAITING_USERNAME) {
            stateService.saveUsernameAndAdvance(telegramUserId, text);
            sender.send(chatId, "✅ Отлично! Теперь введите пароль от журнала:");
        } else if (step == RegistrationStep.AWAITING_PASSWORD) {
            int passwordMessageId = update.getMessage().getMessageId();
            String username = stateService.getPendingUsername(telegramUserId);
            boolean isUpdate = stateService.isUpdate(telegramUserId);
            stateService.clear(telegramUserId);
            sender.deleteMessage(chatId, passwordMessageId);
            try {
                var user = isUpdate
                        ? userClient.updateCredentials(telegramUserId, username, text)
                        : userClient.register(telegramUserId, username, text);
                String displayName = user.fullName() != null ? user.fullName() : user.journalUsername();
                sender.send(chatId, isUpdate
                        ? "✅ Данные обновлены! Теперь всё работает, %s.".formatted(displayName)
                        : "🎉 Регистрация завершена! Добро пожаловать, %s.\nИспользуйте /help, чтобы увидеть доступные команды."
                          .formatted(displayName));
            } catch (HttpClientErrorException.Conflict _) {
                if (isUpdate) {
                    throw new RegistrationFailedException("❌ Этот аккаунт журнала уже привязан к другому пользователю.");
                }
                throw new RegistrationConflictException(username);
            } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized _) {
                throw new RegistrationFailedException("❌ Неверный логин или пароль от журнала.\nПроверьте данные и попробуйте снова.");
            } catch (RestClientException _) {
                throw new RegistrationFailedException();
            }
        }
    }
}
