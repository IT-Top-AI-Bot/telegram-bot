package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.exception.domain.registration.RegistrationConflictException;
import com.aquadev.telegrambot.bot.exception.domain.registration.RegistrationFailedException;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.RegistrationStateService;
import com.aquadev.telegrambot.bot.state.RegistrationStep;
import com.aquadev.telegrambot.client.UserClient;
import com.aquadev.telegrambot.client.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationFlowHandlerTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    UserClient userClient;
    @Mock
    RegistrationStateService stateService;

    RegistrationFlowHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RegistrationFlowHandler(sender, userClient, stateService);
    }

    @Test
    void handle_awaitingUsername_savesAndSendsPasswordPrompt() {
        Update update = mockUpdate(1000L, 42L, "johndoe");
        given(stateService.getStep(42L)).willReturn(RegistrationStep.AWAITING_USERNAME);

        handler.handle(update);

        verify(stateService).saveUsernameAndAdvance(42L, "johndoe");
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(sender).send(eq(1000L), text.capture());
        assertThat(text.getValue()).contains("пароль");
    }

    @Test
    void handle_awaitingPassword_successfulRegistration_deletesPasswordMessageAndSendsWelcome() {
        Update update = mockUpdate(1000L, 42L, "secretpass");
        given(stateService.getStep(42L)).willReturn(RegistrationStep.AWAITING_PASSWORD);
        given(stateService.getPendingUsername(42L)).willReturn("johndoe");
        var userResponse = new UserResponse(UUID.randomUUID(), 42L, "johndoe", null, null);
        given(userClient.register(42L, "johndoe", "secretpass")).willReturn(userResponse);

        handler.handle(update);

        verify(stateService).clear(42L);
        verify(sender).deleteMessage(1000L, 99);
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(sender).send(eq(1000L), text.capture());
        assertThat(text.getValue()).contains("johndoe");
    }

    @Test
    void handle_awaitingPassword_conflictError_throwsRegistrationConflictException() {
        Update update = mockUpdate(1000L, 42L, "pass");
        given(stateService.getStep(42L)).willReturn(RegistrationStep.AWAITING_PASSWORD);
        given(stateService.getPendingUsername(42L)).willReturn("johndoe");
        given(userClient.register(42L, "johndoe", "pass"))
                .willThrow(HttpClientErrorException.Conflict.class);

        assertThatThrownBy(() -> handler.handle(update))
                .isInstanceOf(RegistrationConflictException.class)
                .hasMessageContaining("johndoe");
    }

    @Test
    void handle_awaitingPassword_restClientError_throwsRegistrationFailedException() {
        Update update = mockUpdate(1000L, 42L, "pass");
        given(stateService.getStep(42L)).willReturn(RegistrationStep.AWAITING_PASSWORD);
        given(stateService.getPendingUsername(42L)).willReturn("johndoe");
        given(userClient.register(42L, "johndoe", "pass"))
                .willThrow(new RestClientException("timeout"));

        assertThatThrownBy(() -> handler.handle(update))
                .isInstanceOf(RegistrationFailedException.class);
    }

    @Test
    void handle_awaitingPassword_invalidJournalCredentials_throwsRegistrationFailedWithHint() {
        Update update = mockUpdate(1000L, 42L, "wrongpass");
        given(stateService.getStep(42L)).willReturn(RegistrationStep.AWAITING_PASSWORD);
        given(stateService.getPendingUsername(42L)).willReturn("johndoe");
        given(userClient.register(42L, "johndoe", "wrongpass"))
                .willThrow(HttpClientErrorException.Unauthorized.class);

        assertThatThrownBy(() -> handler.handle(update))
                .isInstanceOf(RegistrationFailedException.class)
                .hasMessageContaining("журнала");
    }

    @Test
    void handle_awaitingPassword_clearsStateAndDeletesMessageBeforeApiCall() {
        Update update = mockUpdate(1000L, 42L, "pass");
        given(stateService.getStep(42L)).willReturn(RegistrationStep.AWAITING_PASSWORD);
        given(stateService.getPendingUsername(42L)).willReturn("johndoe");
        var userResponse = new UserResponse(UUID.randomUUID(), 42L, "johndoe", null, null);
        given(userClient.register(42L, "johndoe", "pass")).willReturn(userResponse);

        handler.handle(update);

        var inOrder = inOrder(stateService, sender, userClient);
        inOrder.verify(stateService).clear(42L);
        inOrder.verify(sender).deleteMessage(1000L, 99);
        inOrder.verify(userClient).register(42L, "johndoe", "pass");
    }

    private Update mockUpdate(long chatId, long userId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User from = mock(User.class);
        given(update.getMessage()).willReturn(message);
        given(message.getChatId()).willReturn(chatId);
        given(message.getMessageId()).willReturn(99);
        given(message.getFrom()).willReturn(from);
        given(from.getId()).willReturn(userId);
        given(message.getText()).willReturn(text);
        return update;
    }
}
