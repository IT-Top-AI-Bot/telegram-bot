package com.aquadev.telegrambot.bot.dispatcher;

import com.aquadev.telegrambot.bot.CommandRegistry;
import com.aquadev.telegrambot.bot.handler.CommandHandler;
import com.aquadev.telegrambot.bot.handler.RegistrationFlowHandler;
import com.aquadev.telegrambot.bot.handler.SubjectSettingsTextInputHandler;
import com.aquadev.telegrambot.bot.state.RegistrationStateService;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateDispatcherTest {

    @Mock
    CommandRegistry commandRegistry;
    @Mock
    CallbackDispatcher callbackDispatcher;
    @Mock
    RegistrationFlowHandler registrationFlowHandler;
    @Mock
    RegistrationStateService registrationStateService;
    @Mock
    SubjectSettingsTextInputHandler subjectSettingsTextInputHandler;
    @Mock
    SubjectSettingsStateService subjectSettingsStateService;
    @Mock
    CommandHandler commandHandler;

    UpdateDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new UpdateDispatcher(commandRegistry, callbackDispatcher,
                registrationFlowHandler, registrationStateService,
                subjectSettingsTextInputHandler, subjectSettingsStateService);
    }

    @Test
    void dispatch_callbackQuery_delegatesToCallbackDispatcher() {
        Update update = mockCallbackUpdate();

        dispatcher.dispatch(update);

        verify(callbackDispatcher).dispatch(update);
        verifyNoInteractions(commandRegistry, registrationFlowHandler);
    }

    @Test
    void dispatch_knownCommand_delegatesToCommandHandler() {
        Update update = mockMessageUpdate(100L, "/start");
        given(commandRegistry.find("/start")).willReturn(commandHandler);

        dispatcher.dispatch(update);

        verify(commandHandler).handle(update);
        verifyNoInteractions(registrationFlowHandler);
    }

    @Test
    void dispatch_unknownText_userInRegistration_delegatesToRegistrationFlow() {
        Update update = mockMessageUpdate(100L, "johndoe");
        given(commandRegistry.find("johndoe")).willReturn(null);
        given(subjectSettingsStateService.isAwaitingInput(100L)).willReturn(false);
        given(registrationStateService.isInProgress(100L)).willReturn(true);

        dispatcher.dispatch(update);

        verify(registrationFlowHandler).handle(update);
        verifyNoInteractions(commandHandler);
    }

    @Test
    void dispatch_unknownText_userNotInRegistration_noHandlerCalled() {
        Update update = mockMessageUpdate(100L, "hello");
        given(commandRegistry.find("hello")).willReturn(null);
        given(subjectSettingsStateService.isAwaitingInput(100L)).willReturn(false);
        given(registrationStateService.isInProgress(100L)).willReturn(false);

        dispatcher.dispatch(update);

        verifyNoInteractions(registrationFlowHandler, commandHandler);
    }

    @Test
    void dispatch_unknownText_userAwaitingSubjectInput_delegatesToSubjectHandler() {
        Update update = mockMessageUpdate(100L, "some prompt text");
        given(commandRegistry.find("some")).willReturn(null);
        given(subjectSettingsStateService.isAwaitingInput(100L)).willReturn(true);

        dispatcher.dispatch(update);

        verify(subjectSettingsTextInputHandler).handle(update);
        verifyNoInteractions(registrationFlowHandler, commandHandler);
    }

    @Test
    void dispatch_updateWithNoMessage_isIgnored() {
        Update update = mock(Update.class);
        given(update.hasCallbackQuery()).willReturn(false);
        given(update.hasMessage()).willReturn(false);

        dispatcher.dispatch(update);

        verifyNoInteractions(commandRegistry, registrationFlowHandler, callbackDispatcher);
    }

    @Test
    void dispatch_commandWithBotMention_stripsAtSign() {
        Update update = mockMessageUpdate(100L, "/start@mybot");
        given(commandRegistry.find("/start")).willReturn(commandHandler);

        dispatcher.dispatch(update);

        verify(commandRegistry).find("/start");
        verify(commandHandler).handle(update);
    }

    // helpers

    private Update mockMessageUpdate(long userId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User from = mock(User.class);

        given(update.hasCallbackQuery()).willReturn(false);
        given(update.hasMessage()).willReturn(true);
        given(update.getMessage()).willReturn(message);
        given(message.hasText()).willReturn(true);
        given(message.getText()).willReturn(text);
        given(message.getFrom()).willReturn(from);
        given(from.getId()).willReturn(userId);

        return update;
    }

    private Update mockCallbackUpdate() {
        Update update = mock(Update.class);
        given(update.hasCallbackQuery()).willReturn(true);
        return update;
    }
}
