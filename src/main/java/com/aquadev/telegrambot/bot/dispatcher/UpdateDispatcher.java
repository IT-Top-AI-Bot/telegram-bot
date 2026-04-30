package com.aquadev.telegrambot.bot.dispatcher;

import com.aquadev.telegrambot.bot.CommandRegistry;
import com.aquadev.telegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.telegrambot.bot.exception.GlobalExceptionHandler;
import com.aquadev.telegrambot.bot.handler.TextUpdateHandler;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.UserClient;
import com.aquadev.telegrambot.client.UserRole;
import com.aquadev.telegrambot.config.properties.AdminProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateDispatcher {

    private final CommandRegistry commandRegistry;
    private final CallbackDispatcher callbackDispatcher;
    private final java.util.List<TextUpdateHandler> textUpdateHandlers;
    private final GlobalExceptionHandler exceptionHandler;
    private final TelegramMessageSender sender;
    private final AdminProperties adminProperties;
    private final UserClient userClient;

    public void dispatch(Update update) {
        log.info("Received update: {}", update.getUpdateId());

        if (update.hasCallbackQuery()) {
            try {
                callbackDispatcher.dispatch(update);
            } catch (Throwable e) {
                exceptionHandler.handle(update, e);
            }
            return;
        }

        if (!update.hasMessage()) {
            log.debug("Update has no message");
            return;
        }

        if (!update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        long telegramUserId = update.getMessage().getFrom().getId();
        log.info("Received message: '{}' from user: {}", text, telegramUserId);

        String command = text.split("\\s+")[0].split("@")[0];
        var handler = commandRegistry.find(command);

        if (handler != null) {
            TelegramBotCommand annotation = commandRegistry.getAnnotation(command);
            if (annotation != null && requiresAdmin(annotation) && !adminProperties.isAdmin(telegramUserId)) {
                log.warn("Access denied for user {} to command {}", telegramUserId, command);
                sender.send(chatId, "⛔ У вас нет прав для выполнения этой команды.");
                return;
            }
            if (annotation != null && annotation.requiresValidCredentials()) {
                try {
                    var user = userClient.getMe(telegramUserId);
                    if (user.isPresent() && user.get().credentialsInvalid()) {
                        log.warn("Blocked command '{}' for telegramId={}: credentials invalid", command, telegramUserId);
                        exceptionHandler.handleCredentialsInvalid(update);
                        return;
                    }
                } catch (HttpClientErrorException.Forbidden e) {
                    log.warn("Blocked command '{}' for telegramId={}: 403 from users/me", command, telegramUserId);
                    exceptionHandler.handleCredentialsInvalid(update);
                    return;
                }
            }
            log.info("Dispatching to command handler: {}", handler.getClass().getSimpleName());
            try {
                handler.handle(update);
            } catch (Throwable e) {
                exceptionHandler.handle(update, e);
            }
        } else if (dispatchTextUpdate(update)) {
        } else {
            log.info("No handler found for command: {}", command);
        }
    }

    private boolean dispatchTextUpdate(Update update) {
        for (TextUpdateHandler handler : textUpdateHandlers) {
            if (!handler.supports(update)) {
                continue;
            }
            log.info("Dispatching text update to handler: {}", handler.getClass().getSimpleName());
            try {
                handler.handle(update);
            } catch (Throwable e) {
                exceptionHandler.handle(update, e);
            }
            return true;
        }
        return false;
    }

    private boolean requiresAdmin(TelegramBotCommand annotation) {
        return Arrays.asList(annotation.roles()).contains(UserRole.ADMIN);
    }
}
