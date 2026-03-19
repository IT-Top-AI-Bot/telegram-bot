package com.aquadev.ittopaitelegrambot.bot.callback.subjectsettings;

import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import com.aquadev.ittopaitelegrambot.client.ExecutorClient;
import com.aquadev.ittopaitelegrambot.client.dto.SubjectPromptDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubjectSettingsViewTextCallbackTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    ExecutorClient executorClient;

    SubjectSettingsViewTextCallback callback;

    @BeforeEach
    void setUp() {
        callback = new SubjectSettingsViewTextCallback(sender, executorClient);
    }

    @Test
    void supports_returnsTrue_forViewPrompt() {
        assertThat(callback.supports(SubjectSettingsCallbackData.VIEW_PROMPT + "1")).isTrue();
    }

    @Test
    void supports_returnsTrue_forViewStatic() {
        assertThat(callback.supports(SubjectSettingsCallbackData.VIEW_STATIC + "1")).isTrue();
    }

    @Test
    void supports_returnsFalse_forOther() {
        assertThat(callback.supports(SubjectSettingsCallbackData.OPEN)).isFalse();
    }

    @Test
    void handle_viewPrompt_withPromptText_showsAlert() {
        var prompt = new SubjectPromptDto(1L, "Math", "my custom prompt", null, null);
        Update update = mockUpdate(42L, SubjectSettingsCallbackData.VIEW_PROMPT + "1");

        given(executorClient.getPromptBySpecId(42L, 1L)).willReturn(Optional.of(prompt));

        callback.handle(update);

        verify(sender).answerCallbackAlert("cb-id", "my custom prompt");
    }

    @Test
    void handle_viewStatic_withStaticText_showsAlert() {
        var prompt = new SubjectPromptDto(1L, "Math", null, null, "static answer");
        Update update = mockUpdate(42L, SubjectSettingsCallbackData.VIEW_STATIC + "1");

        given(executorClient.getPromptBySpecId(42L, 1L)).willReturn(Optional.of(prompt));

        callback.handle(update);

        verify(sender).answerCallbackAlert("cb-id", "static answer");
    }

    @Test
    void handle_viewPrompt_textIsNull_answersWithNotSet() {
        var prompt = new SubjectPromptDto(1L, "Math", null, null, null);
        Update update = mockUpdate(42L, SubjectSettingsCallbackData.VIEW_PROMPT + "1");

        given(executorClient.getPromptBySpecId(42L, 1L)).willReturn(Optional.of(prompt));

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), contains("не задан"));
    }

    @Test
    void handle_noPrompt_answersWithNotSet() {
        Update update = mockUpdate(42L, SubjectSettingsCallbackData.VIEW_PROMPT + "1");

        given(executorClient.getPromptBySpecId(42L, 1L)).willReturn(Optional.empty());

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), contains("не задан"));
    }

    private Update mockUpdate(long userId, String data) {
        Update update = mock(Update.class);
        CallbackQuery cq = mock(CallbackQuery.class);
        User from = mock(User.class);

        given(update.getCallbackQuery()).willReturn(cq);
        given(cq.getId()).willReturn("cb-id");
        given(cq.getData()).willReturn(data);
        given(cq.getFrom()).willReturn(from);
        given(from.getId()).willReturn(userId);
        return update;
    }
}
