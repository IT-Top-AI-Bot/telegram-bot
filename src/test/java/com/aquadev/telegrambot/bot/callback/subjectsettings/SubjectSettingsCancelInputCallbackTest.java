package com.aquadev.telegrambot.bot.callback.subjectsettings;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService;
import com.aquadev.telegrambot.client.ExecutorClient;
import com.aquadev.telegrambot.client.dto.SubjectPromptDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubjectSettingsCancelInputCallbackTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    SubjectSettingsStateService stateService;
    @Mock
    ExecutorClient executorClient;

    SubjectSettingsCancelInputCallback callback;

    @BeforeEach
    void setUp() {
        callback = new SubjectSettingsCancelInputCallback(sender, stateService, executorClient);
    }

    @Test
    void supports_returnsTrue_forCancelInput() {
        assertThat(callback.supports(SubjectSettingsCallbackData.CANCEL_INPUT)).isTrue();
    }

    @Test
    void supports_returnsFalse_forOther() {
        assertThat(callback.supports(SubjectSettingsCallbackData.OPEN)).isFalse();
    }

    @Test
    void handle_inaccessibleMessage_answersWithError() {
        Update update = mockCallbackUpdate(42L, false);

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), contains("устарело"));
    }

    @Test
    void handle_nullState_answersCallbackAndReturns() {
        Update update = mockCallbackUpdate(42L, true);
        given(stateService.getState(42L)).willReturn(null);

        callback.handle(update);

        verify(sender).answerCallback("cb-id");
    }

    @Test
    void handle_withState_restoresSpecDetailView() {
        var state = new SubjectSettingsStateService.InputState(
                10L, "Math", 200L, 5, SubjectSettingsStateService.InputType.SYSTEM_PROMPT);
        var prompt = new SubjectPromptDto(10L, "Math", "my prompt", null, null);

        Update update = mockCallbackUpdate(42L, true);
        given(stateService.getState(42L)).willReturn(state);
        given(executorClient.getPromptBySpecId(42L, 10L)).willReturn(Optional.of(prompt));

        callback.handle(update);

        verify(stateService).clear(42L);
        verify(sender).answerCallback("cb-id");
        verify(sender).editHtml(eq(200L), eq(5), any(), any());
    }

    @Test
    void handle_withState_noPrompt_restoresSpecDetailView() {
        var state = new SubjectSettingsStateService.InputState(
                10L, "Math", 200L, 5, SubjectSettingsStateService.InputType.STATIC_TEXT);

        Update update = mockCallbackUpdate(42L, true);
        given(stateService.getState(42L)).willReturn(state);
        given(executorClient.getPromptBySpecId(42L, 10L)).willReturn(Optional.empty());

        callback.handle(update);

        verify(sender).editHtml(eq(200L), eq(5), any(), any());
    }

    private Update mockCallbackUpdate(long userId, boolean accessible) {
        Update update = mock(Update.class);
        CallbackQuery cq = mock(CallbackQuery.class);
        User from = mock(User.class);

        given(update.getCallbackQuery()).willReturn(cq);
        given(cq.getId()).willReturn("cb-id");
        given(cq.getData()).willReturn(SubjectSettingsCallbackData.CANCEL_INPUT);
        given(cq.getFrom()).willReturn(from);
        given(from.getId()).willReturn(userId);

        if (accessible) {
            Message message = mock(Message.class);
            given(cq.getMessage()).willReturn(message);
        } else {
            given(cq.getMessage()).willReturn(null);
        }
        return update;
    }
}

