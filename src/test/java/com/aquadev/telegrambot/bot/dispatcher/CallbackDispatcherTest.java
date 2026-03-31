package com.aquadev.telegrambot.bot.dispatcher;

import com.aquadev.telegrambot.bot.callback.CallbackHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallbackDispatcherTest {

    @Mock
    CallbackHandler handlerA;
    @Mock
    CallbackHandler handlerB;

    CallbackDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new CallbackDispatcher(List.of(handlerA, handlerB));
    }

    @Test
    void dispatch_callsMatchingHandler() {
        Update update = mockCallbackUpdate("data:abc");
        given(handlerA.supports("data:abc")).willReturn(false);
        given(handlerB.supports("data:abc")).willReturn(true);

        dispatcher.dispatch(update);

        verify(handlerB).handle(update);
        verify(handlerA, never()).handle(any());
    }

    @Test
    void dispatch_noMatchingHandler_nothingCalled() {
        Update update = mockCallbackUpdate("unknown");
        given(handlerA.supports("unknown")).willReturn(false);
        given(handlerB.supports("unknown")).willReturn(false);

        dispatcher.dispatch(update);

        verify(handlerA, never()).handle(any());
        verify(handlerB, never()).handle(any());
    }

    @Test
    void dispatch_nullData_nothingCalled() {
        Update update = mock(Update.class);
        CallbackQuery callback = mock(CallbackQuery.class);
        given(update.getCallbackQuery()).willReturn(callback);
        given(callback.getData()).willReturn(null);

        dispatcher.dispatch(update);

        verifyNoInteractions(handlerA, handlerB);
    }

    @Test
    void dispatch_firstMatchWins() {
        Update update = mockCallbackUpdate("data");
        given(handlerA.supports("data")).willReturn(true);

        dispatcher.dispatch(update);

        verify(handlerA).handle(update);
        verifyNoMoreInteractions(handlerB);
    }

    private Update mockCallbackUpdate(String data) {
        Update update = mock(Update.class);
        CallbackQuery callback = mock(CallbackQuery.class);
        given(update.getCallbackQuery()).willReturn(callback);
        given(callback.getData()).willReturn(data);
        return update;
    }
}
