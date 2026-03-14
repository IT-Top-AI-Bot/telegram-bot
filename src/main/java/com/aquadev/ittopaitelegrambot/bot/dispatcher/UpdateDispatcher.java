package com.aquadev.ittopaitelegrambot.bot.dispatcher;

import com.aquadev.ittopaitelegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.ittopaitelegrambot.bot.handler.CommandHandler;
import com.aquadev.ittopaitelegrambot.bot.handler.RegistrationFlowHandler;
import com.aquadev.ittopaitelegrambot.bot.state.RegistrationStateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateDispatcher {

    private final List<CommandHandler> handlers;
    private final RegistrationFlowHandler registrationFlowHandler;
    private final RegistrationStateService registrationStateService;
    private final Map<String, CommandHandler> commandMap = new HashMap<>();

    @PostConstruct
    void buildCommandMap() {
        for (CommandHandler handler : handlers) {
            TelegramBotCommand annotation = AopUtils.getTargetClass(handler).getAnnotation(TelegramBotCommand.class);
            if (annotation != null) {
                commandMap.put(annotation.value(), handler);
            }
        }
    }

    public void dispatch(Update update) {
        log.info("Received update: {}", update.getUpdateId());
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            log.debug("Update has no message or text");
            return;
        }

        String text = update.getMessage().getText();
        long telegramUserId = update.getMessage().getFrom().getId();
        log.info("Received message: '{}' from user: {}", text, telegramUserId);
        String command = text.split("\\s+")[0].split("@")[0];

        CommandHandler handler = commandMap.get(command);
        if (handler != null) {
            log.info("Dispatching to command handler async: {}", handler.getClass().getSimpleName());
            CompletableFuture.runAsync(() -> handler.handle(update));
        } else if (registrationStateService.isInProgress(telegramUserId)) {
            log.info("Dispatching to registration flow handler async for user: {}", telegramUserId);
            CompletableFuture.runAsync(() -> registrationFlowHandler.handle(update));
        } else {
            log.info("No handler found for command: {}", command);
        }
    }
}
