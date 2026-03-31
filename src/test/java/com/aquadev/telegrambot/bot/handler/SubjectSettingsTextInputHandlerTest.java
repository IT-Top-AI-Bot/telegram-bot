package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService.InputState;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService.InputType;
import com.aquadev.telegrambot.client.ExecutorClient;
import com.aquadev.telegrambot.client.dto.SubjectPromptDto;
import com.aquadev.telegrambot.client.dto.UpsertSubjectPromptRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubjectSettingsTextInputHandlerTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    SubjectSettingsStateService stateService;
    @Mock
    ExecutorClient executorClient;

    SubjectSettingsTextInputHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SubjectSettingsTextInputHandler(sender, stateService, executorClient);
    }

    @Test
    void handle_nullState_deletesMessageAndReturns() {
        Update update = mockTextUpdate(42L, 100L, 5, "some text");
        given(stateService.getState(42L)).willReturn(null);

        handler.handle(update);

        verify(sender).deleteMessage(100L, 5);
        verify(stateService).clear(42L);
    }

    @Test
    void handle_systemPromptInput_upsertsWithTextAsPrompt() {
        var state = new InputState(10L, "Math", 200L, 9, InputType.SYSTEM_PROMPT);
        var saved = new SubjectPromptDto(10L, "Math", "my input", null, null);

        Update update = mockTextUpdate(42L, 100L, 5, "my input");
        given(stateService.getState(42L)).willReturn(state);
        given(executorClient.getPromptBySpecId(42L, 10L)).willReturn(Optional.empty());
        given(executorClient.upsertPrompt(eq(42L), eq(10L), any())).willReturn(saved);

        handler.handle(update);

        ArgumentCaptor<UpsertSubjectPromptRequest> captor = ArgumentCaptor.forClass(UpsertSubjectPromptRequest.class);
        verify(executorClient).upsertPrompt(eq(42L), eq(10L), captor.capture());
        assertThat(captor.getValue().systemPrompt()).isEqualTo("my input");
        assertThat(captor.getValue().staticText()).isNull();

        verify(sender).editHtml(eq(200L), eq(9), any(), any());
    }

    @Test
    void handle_staticTextInput_upsertsWithTextAsStatic() {
        var state = new InputState(10L, "Math", 200L, 9, InputType.STATIC_TEXT);
        var saved = new SubjectPromptDto(10L, "Math", null, null, "my static");

        Update update = mockTextUpdate(42L, 100L, 5, "my static");
        given(stateService.getState(42L)).willReturn(state);
        given(executorClient.getPromptBySpecId(42L, 10L)).willReturn(Optional.empty());
        given(executorClient.upsertPrompt(eq(42L), eq(10L), any())).willReturn(saved);

        handler.handle(update);

        ArgumentCaptor<UpsertSubjectPromptRequest> captor = ArgumentCaptor.forClass(UpsertSubjectPromptRequest.class);
        verify(executorClient).upsertPrompt(eq(42L), eq(10L), captor.capture());
        assertThat(captor.getValue().staticText()).isEqualTo("my static");
        assertThat(captor.getValue().systemPrompt()).isNull();
    }

    @Test
    void handle_systemPromptInput_preservesExistingStaticText() {
        var state = new InputState(10L, "Math", 200L, 9, InputType.SYSTEM_PROMPT);
        var existing = new SubjectPromptDto(10L, "Math", null, null, "preserved static");
        var saved = new SubjectPromptDto(10L, "Math", "new prompt", null, "preserved static");

        Update update = mockTextUpdate(42L, 100L, 5, "new prompt");
        given(stateService.getState(42L)).willReturn(state);
        given(executorClient.getPromptBySpecId(42L, 10L)).willReturn(Optional.of(existing));
        given(executorClient.upsertPrompt(eq(42L), eq(10L), any())).willReturn(saved);

        handler.handle(update);

        ArgumentCaptor<UpsertSubjectPromptRequest> captor = ArgumentCaptor.forClass(UpsertSubjectPromptRequest.class);
        verify(executorClient).upsertPrompt(eq(42L), eq(10L), captor.capture());
        assertThat(captor.getValue().staticText()).isEqualTo("preserved static");
    }

    @Test
    void handle_staticTextInput_preservesExistingPrompt() {
        var state = new InputState(10L, "Math", 200L, 9, InputType.STATIC_TEXT);
        var existing = new SubjectPromptDto(10L, "Math", "existing prompt", null, null);
        var saved = new SubjectPromptDto(10L, "Math", "existing prompt", null, "new static");

        Update update = mockTextUpdate(42L, 100L, 5, "new static");
        given(stateService.getState(42L)).willReturn(state);
        given(executorClient.getPromptBySpecId(42L, 10L)).willReturn(Optional.of(existing));
        given(executorClient.upsertPrompt(eq(42L), eq(10L), any())).willReturn(saved);

        handler.handle(update);

        ArgumentCaptor<UpsertSubjectPromptRequest> captor = ArgumentCaptor.forClass(UpsertSubjectPromptRequest.class);
        verify(executorClient).upsertPrompt(eq(42L), eq(10L), captor.capture());
        assertThat(captor.getValue().systemPrompt()).isEqualTo("existing prompt");
    }

    private Update mockTextUpdate(long userId, long chatId, int messageId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User from = mock(User.class);

        given(update.getMessage()).willReturn(message);
        given(message.getFrom()).willReturn(from);
        given(from.getId()).willReturn(userId);
        given(message.getChatId()).willReturn(chatId);
        given(message.getMessageId()).willReturn(messageId);
        given(message.getText()).willReturn(text);
        return update;
    }
}
