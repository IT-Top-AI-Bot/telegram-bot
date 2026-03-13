package com.aquadev.ittopaitelegrambot.bot.dispatcher;

import com.aquadev.ittopaitelegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.ittopaitelegrambot.bot.handler.CommandHandler;
import com.aquadev.ittopaitelegrambot.bot.handler.RegistrationFlowHandler;
import com.aquadev.ittopaitelegrambot.bot.state.RegistrationStateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText();
        long telegramUserId = update.getMessage().getFrom().getId();
        String command = text.split("\\s+")[0].split("@")[0];

        CommandHandler handler = commandMap.get(command);
        if (handler != null) {
            handler.handle(update);
        } else if (registrationStateService.isInProgress(telegramUserId)) {
            registrationFlowHandler.handle(update);
        }
    }
}
