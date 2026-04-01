package com.aquadev.telegrambot.bot.callback.autohomework;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.AutoHomeworkStateService;
import com.aquadev.telegrambot.client.AutoHomeworkClient;
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
class AutoHomeworkSpecToggleAllCallbackTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    AutoHomeworkClient client;
    @Mock
    AutoHomeworkStateService stateService;

    AutoHomeworkSpecToggleAllCallback callback;

    @BeforeEach
    void setUp() {
        callback = new AutoHomeworkSpecToggleAllCallback(sender, client, stateService);
    }

    @Test
    void supports_returnsTrue_forSpecToggleAll() {
        assertThat(callback.supports(AutoHomeworkCallbackData.SPEC_TOGGLE_ALL)).isTrue();
    }

    @Test
    void supports_returnsFalse_forOtherData() {
        assertThat(callback.supports(AutoHomeworkCallbackData.SPEC_TOGGLE + "1")).isFalse();
    }

    @Test
    void handle_notAllSelected_selectsAll() {
        var specs = List.of(
                new JournalSpecResponse(1L, "Math", "MTH"),
                new JournalSpecResponse(2L, "Physics", "PHY")
        );
        Update update = mockCallbackUpdate(10L, 200L, 3);
        given(client.getGroupSpecs(10L)).willReturn(specs);
        given(stateService.getPendingSpecIds(10L)).willReturn(Set.of(1L));

        callback.handle(update);

        verify(stateService).setSpecs(10L, Set.of(1L, 2L));
        verify(sender).answerCallback("cb-id");
        verify(sender).editMarkup(eq(200L), eq(3), any());
    }

    @Test
    void handle_allSelected_deselectsAll() {
        var specs = List.of(
                new JournalSpecResponse(1L, "Math", "MTH"),
                new JournalSpecResponse(2L, "Physics", "PHY")
        );
        Update update = mockCallbackUpdate(10L, 200L, 3);
        given(client.getGroupSpecs(10L)).willReturn(specs);
        given(stateService.getPendingSpecIds(10L)).willReturn(Set.of(1L, 2L));

        callback.handle(update);

        verify(stateService).setSpecs(10L, Set.of());
        verify(sender).answerCallback("cb-id");
        verify(sender).editMarkup(eq(200L), eq(3), any());
    }

    @Test
    void handle_noneSelected_selectsAll() {
        var specs = List.of(new JournalSpecResponse(5L, "History", "HIS"));
        Update update = mockCallbackUpdate(10L, 200L, 3);
        given(client.getGroupSpecs(10L)).willReturn(specs);
        given(stateService.getPendingSpecIds(10L)).willReturn(Set.of());

        callback.handle(update);

        verify(stateService).setSpecs(10L, Set.of(5L));
    }

    @Test
    void handle_inaccessibleMessage_answersWithError() {
        Update update = mock(Update.class);
        CallbackQuery cq = mock(CallbackQuery.class);
        given(update.getCallbackQuery()).willReturn(cq);
        given(cq.getId()).willReturn("cb-id");
        given(cq.getData()).willReturn(AutoHomeworkCallbackData.SPEC_TOGGLE_ALL);
        given(cq.getMessage()).willReturn(null);

        callback.handle(update);

        verify(sender).answerCallback(eq("cb-id"), contains("устарело"));
        verifyNoInteractions(client, stateService);
    }

    private Update mockCallbackUpdate(long userId, long chatId, int messageId) {
        Update update = mock(Update.class);
        CallbackQuery cq = mock(CallbackQuery.class);
        User from = mock(User.class);
        Message message = mock(Message.class);

        given(update.getCallbackQuery()).willReturn(cq);
        given(cq.getId()).willReturn("cb-id");
        given(cq.getData()).willReturn(AutoHomeworkCallbackData.SPEC_TOGGLE_ALL);
        given(cq.getFrom()).willReturn(from);
        given(from.getId()).willReturn(userId);
        given(cq.getMessage()).willReturn(message);
        given(message.getChatId()).willReturn(chatId);
        given(message.getMessageId()).willReturn(messageId);
        return update;
    }
}
