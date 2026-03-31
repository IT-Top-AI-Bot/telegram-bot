package com.aquadev.telegrambot.bot.callback.autohomework;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.AutoHomeworkStateService;
import com.aquadev.telegrambot.client.AutoHomeworkClient;
import com.aquadev.telegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
import com.aquadev.telegrambot.client.dto.UpdateAutoHomeworkSettingsRequest;
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

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AutoHomeworkSpecSaveCallbackTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    AutoHomeworkClient client;
    @Mock
    AutoHomeworkStateService stateService;
    @Mock
    AutoHomeworkSettingsService settingsService;

    AutoHomeworkSpecSaveCallback callback;

    @BeforeEach
    void setUp() {
        callback = new AutoHomeworkSpecSaveCallback(sender, client, stateService, settingsService);
    }

    @Test
    void supports_returnsTrue_forSpecSave() {
        assertThat(callback.supports(AutoHomeworkCallbackData.SPEC_SAVE)).isTrue();
    }

    @Test
    void supports_returnsFalse_forOtherData() {
        assertThat(callback.supports(AutoHomeworkCallbackData.CANCEL)).isFalse();
    }

    @Test
    void handle_savesPendingSpecsAndRefreshesSettings() {
        Set<Long> pending = Set.of(10L, 20L);
        var current = new AutoHomeworkSettingsResponse(true, null, Set.of(5L));
        var updated = new AutoHomeworkSettingsResponse(true, null, pending);
        var specs = List.of(new JournalSpecResponse(10L, "Math", "MTH"));
        Update update = mockCallbackUpdate(100L, 300L, 8);

        given(stateService.getPendingSpecIds(100L)).willReturn(pending);
        given(client.getSettings(100L)).willReturn(current);
        given(client.updateSettings(eq(100L), any(UpdateAutoHomeworkSettingsRequest.class))).willReturn(updated);
        given(client.getGroupSpecs(100L)).willReturn(specs);

        callback.handle(update);

        verify(stateService).clear(100L);
        verify(sender).answerCallback(eq("cb-id"), any());
        verify(settingsService).editSettingsMessage(eq(300L), eq(8), eq(updated), eq(specs));
    }

    private Update mockCallbackUpdate(long userId, long chatId, int messageId) {
        Update update = org.mockito.Mockito.mock(Update.class);
        CallbackQuery cq = org.mockito.Mockito.mock(CallbackQuery.class);
        Message message = org.mockito.Mockito.mock(Message.class);
        User from = org.mockito.Mockito.mock(User.class);

        given(update.getCallbackQuery()).willReturn(cq);
        given(cq.getId()).willReturn("cb-id");
        given(cq.getData()).willReturn(AutoHomeworkCallbackData.SPEC_SAVE);
        given(cq.getFrom()).willReturn(from);
        given(cq.getMessage()).willReturn(message);
        given(from.getId()).willReturn(userId);
        given(message.getChatId()).willReturn(chatId);
        given(message.getMessageId()).willReturn(messageId);
        return update;
    }
}
