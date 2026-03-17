package com.aquadev.ittopaitelegrambot.bot.callback.autohomework;

import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import com.aquadev.ittopaitelegrambot.client.AutoHomeworkClient;
import com.aquadev.ittopaitelegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.ittopaitelegrambot.client.dto.JournalSpecResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AutoHomeworkSettingsServiceTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    AutoHomeworkClient client;

    AutoHomeworkSettingsService service;

    private final AutoHomeworkSettingsResponse settings =
            new AutoHomeworkSettingsResponse(true, null, Set.of(1L));
    private final List<JournalSpecResponse> specs =
            List.of(new JournalSpecResponse(1L, "Math", "MTH"));

    @BeforeEach
    void setUp() {
        service = new AutoHomeworkSettingsService(sender, client);
    }

    @Test
    void sendSettingsMessage_fetchesDataAndCallsSendHtml() {
        given(client.getSettings(10L)).willReturn(settings);
        given(client.getGroupSpecs(10L)).willReturn(specs);

        service.sendSettingsMessage(200L, 10L);

        verify(client).getSettings(10L);
        verify(client).getGroupSpecs(10L);
        verify(sender).sendHtml(eq(200L), contains("Авто-домашки"), any());
    }

    @Test
    void editSettingsMessage_byUserId_fetchesDataAndCallsEditHtml() {
        given(client.getSettings(10L)).willReturn(settings);
        given(client.getGroupSpecs(10L)).willReturn(specs);

        service.editSettingsMessage(200L, 5, 10L);

        verify(client).getSettings(10L);
        verify(client).getGroupSpecs(10L);
        verify(sender).editHtml(eq(200L), eq(5), contains("Авто-домашки"), any());
    }

    @Test
    void editSettingsMessage_withGivenData_callsEditHtmlDirectly() {
        service.editSettingsMessage(200L, 5, settings, specs);

        verify(sender).editHtml(eq(200L), eq(5), contains("Авто-домашки"), any());
        verifyNoInteractions(client);
    }
}
