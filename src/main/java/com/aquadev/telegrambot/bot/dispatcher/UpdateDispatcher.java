package com.aquadev.telegrambot.bot.dispatcher;

import com.aquadev.telegrambot.bot.CommandRegistry;
import com.aquadev.telegrambot.bot.exception.GlobalExceptionHandler;
import com.aquadev.telegrambot.bot.handler.RegistrationFlowHandler;
import com.aquadev.telegrambot.bot.handler.SubjectSettingsTextInputHandler;
import com.aquadev.telegrambot.bot.state.RegistrationStateService;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateDispatcher {

    private final CommandRegistry commandRegistry;
    private final CallbackDispatcher callbackDispatcher;
    private final RegistrationFlowHandler registrationFlowHandler;
    private final RegistrationStateService registrationStateService;
    private final SubjectSettingsTextInputHandler subjectSettingsTextInputHandler;
    private final SubjectSettingsStateService subjectSettingsStateService;
    private final GlobalExceptionHandler exceptionHandler;

    public void dispatch(Update update) {
        log.info("Received update: {}", update.getUpdateId());

        if (update.hasCallbackQuery()) {
            callbackDispatcher.dispatch(update);
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            log.debug("Update has no message or text");
            return;
        }

        String text = update.getMessage().getText();
        long telegramUserId = update.getMessage().getFrom().getId();
        log.info("Received message: '{}' from user: {}", text, telegramUserId);

        String command = text.split("\\s+")[0].split("@")[0];
        var handler = commandRegistry.find(command);

        if (handler != null) {
            log.info("Dispatching to command handler: {}", handler.getClass().getSimpleName());
            handler.handle(update);
        } else if (subjectSettingsStateService.isAwaitingInput(telegramUserId)) {
            log.info("Dispatching to subject settings text input for user: {}", telegramUserId);
            try {
                subjectSettingsTextInputHandler.handle(update);
            } catch (Throwable e) {
                exceptionHandler.handle(update, e);
            }
        } else if (registrationStateService.isInProgress(telegramUserId)) {
            log.info("Dispatching to registration flow for user: {}", telegramUserId);
            try {
                registrationFlowHandler.handle(update);
            } catch (Throwable e) {
                exceptionHandler.handle(update, e);
            }
        } else {
            log.info("No handler found for command: {}", command);
        }
    }
}
