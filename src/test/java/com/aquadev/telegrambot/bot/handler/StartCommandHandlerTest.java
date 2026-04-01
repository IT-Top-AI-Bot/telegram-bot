package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.RegistrationStateService;
import com.aquadev.telegrambot.client.UserClient;
import com.aquadev.telegrambot.client.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartCommandHandlerTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    UserClient userClient;
    @Mock
    RegistrationStateService stateService;

    StartCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new StartCommandHandler(sender, userClient, stateService);
    }

    @Test
    void handle_existingUser_sendsWelcomeWithUsername() {
        Update update = mockUpdate(1000L, 42L);
        var user = new UserResponse(UUID.randomUUID(), 42L, "johndoe", null, null);
        given(userClient.getMe(42L)).willReturn(Optional.of(user));

        handler.handle(update);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(sender).send(eq(1000L), textCaptor.capture());
        assertThat(textCaptor.getValue()).contains("johndoe");
    }

    @Test
    void handle_newUser_startsRegistrationAndSendsPrompt() {
        Update update = mockUpdate(1000L, 42L);
        given(userClient.getMe(42L)).willReturn(Optional.empty());

        handler.handle(update);

        verify(stateService).start(42L);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(sender).send(eq(1000L), textCaptor.capture());
        assertThat(textCaptor.getValue()).contains("логин");
    }

    private Update mockUpdate(long chatId, long userId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User from = mock(User.class);
        given(update.getMessage()).willReturn(message);
        given(message.getChatId()).willReturn(chatId);
        given(message.getFrom()).willReturn(from);
        given(from.getId()).willReturn(userId);
        return update;
    }
}
