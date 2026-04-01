package com.aquadev.telegrambot.bot.callback.autohomework;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.AutoHomeworkClient;
import com.aquadev.telegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
import com.aquadev.telegrambot.client.dto.UpdateAutoHomeworkSettingsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AutoHomeworkSetEnabledCallbackTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    AutoHomeworkClient client;
    @Mock
    AutoHomeworkSettingsService settingsService;

    AutoHomeworkSetEnabledCallback callback;

    @BeforeEach
    void setUp() {
        callback = new AutoHomeworkSetEnabledCallback(sender, client, settingsService);
    }

    @Test
    void supports_returnsTrue_forSetEnabledPrefix() {
        assertThat(callback.supports(AutoHomeworkCallbackData.SET_ENABLED + "true")).isTrue();
        assertThat(callback.supports(AutoHomeworkCallbackData.SET_ENABLED + "false")).isTrue();
    }

    @Test
    void supports_returnsFalse_forOtherData() {
        assertThat(callback.supports(AutoHomeworkCallbackData.OPEN_SPECS)).isFalse();
        assertThat(callback.supports("something")).isFalse();
    }

    @Test
    void handle_enable_updatesSettingsAndAnswersCallback() {
        var current = new AutoHomeworkSettingsResponse(false, null, Set.of(1L));
        var updated = new AutoHomeworkSettingsResponse(true, null, Set.of(1L));
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        var update = mockCallbackUpdate(AutoHomeworkCallbackData.SET_ENABLED + "true", 100L, 300L, 5);

        given(client.getSettings(100L)).willReturn(current);
        given(client.updateSettings(eq(100L), any(UpdateAutoHomeworkSettingsRequest.class))).willReturn(updated);
        given(client.getGroupSpecs(100L)).willReturn(specs);

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), any());
        verify(settingsService).editSettingsMessage(eq(300L), eq(5), eq(updated), eq(specs));
    }

    @Test
    void handle_disable_updatesSettingsWithEnabledFalse() {
        var current = new AutoHomeworkSettingsResponse(true, null, Set.of());
        var updated = new AutoHomeworkSettingsResponse(false, null, Set.of());
        var update = mockCallbackUpdate(AutoHomeworkCallbackData.SET_ENABLED + "false", 100L, 300L, 5);

        given(client.getSettings(100L)).willReturn(current);
        given(client.updateSettings(eq(100L), any(UpdateAutoHomeworkSettingsRequest.class))).willReturn(updated);
        given(client.getGroupSpecs(100L)).willReturn(List.of());

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), any());
    }

    private Update mockCallbackUpdate(String data, long userId, long chatId, int messageId) {
        Update update = org.mockito.Mockito.mock(Update.class);
        CallbackQuery cq = org.mockito.Mockito.mock(CallbackQuery.class);
        Message message = org.mockito.Mockito.mock(Message.class);
        User from = org.mockito.Mockito.mock(User.class);

        given(update.getCallbackQuery()).willReturn(cq);
        given(cq.getId()).willReturn("cb-id");
        given(cq.getData()).willReturn(data);
        given(cq.getFrom()).willReturn(from);
        given(cq.getMessage()).willReturn(message);
        given(from.getId()).willReturn(userId);
        given(message.getChatId()).willReturn(chatId);
        given(message.getMessageId()).willReturn(messageId);
        return update;
    }
}

