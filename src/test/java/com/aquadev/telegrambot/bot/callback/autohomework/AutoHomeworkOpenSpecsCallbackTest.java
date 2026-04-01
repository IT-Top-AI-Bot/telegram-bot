package com.aquadev.telegrambot.bot.callback.autohomework;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.AutoHomeworkStateService;
import com.aquadev.telegrambot.client.AutoHomeworkClient;
import com.aquadev.telegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AutoHomeworkOpenSpecsCallbackTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    AutoHomeworkClient client;
    @Mock
    AutoHomeworkStateService stateService;

    AutoHomeworkOpenSpecsCallback callback;

    @BeforeEach
    void setUp() {
        callback = new AutoHomeworkOpenSpecsCallback(sender, client, stateService);
    }

    @Test
    void supports_returnsTrue_forOpenSpecs() {
        assertThat(callback.supports(AutoHomeworkCallbackData.OPEN_SPECS)).isTrue();
    }

    @Test
    void supports_returnsFalse_forOtherData() {
        assertThat(callback.supports(AutoHomeworkCallbackData.SET_ENABLED + "true")).isFalse();
    }

    @Test
    void handle_accessibleMessage_initializesStateAndShowsSpecKeyboard() {
        var settings = new AutoHomeworkSettingsResponse(true, null, Set.of(1L));
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        Update update = mockCallbackUpdate(AutoHomeworkCallbackData.OPEN_SPECS, 42L, 100L, 7, true);

        given(client.getSettings(42L)).willReturn(settings);
        given(client.getGroupSpecs(42L)).willReturn(specs);
        given(stateService.getPendingSpecIds(42L)).willReturn(Set.of(1L));

        callback.handle(update);

        verify(stateService).startSpecSelection(42L, Set.of(1L));
        verify(sender).answerCallback("cb-id");
        verify(sender).editHtml(eq(100L), eq(7), contains("Предметы"), any());
    }

    @Test
    void handle_inaccessibleMessage_answersWithErrorAndReturns() {
        Update update = mockCallbackUpdate(AutoHomeworkCallbackData.OPEN_SPECS, 42L, 0L, 0, false);

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), contains("устарело"));
        verifyNoInteractions(client, stateService);
    }

    private Update mockCallbackUpdate(String data, long userId, long chatId, int messageId, boolean accessible) {
        Update update = mock(Update.class);
        CallbackQuery cq = mock(CallbackQuery.class);
        User from = mock(User.class);

        given(update.getCallbackQuery()).willReturn(cq);
        given(cq.getId()).willReturn("cb-id");
        given(cq.getData()).willReturn(data);
        given(cq.getFrom()).willReturn(from);
        given(from.getId()).willReturn(userId);

        if (accessible) {
            Message message = mock(Message.class);
            given(cq.getMessage()).willReturn(message);
            given(message.getChatId()).willReturn(chatId);
            given(message.getMessageId()).willReturn(messageId);
        } else {
            given(cq.getMessage()).willReturn(null);
        }
        return update;
    }
}

