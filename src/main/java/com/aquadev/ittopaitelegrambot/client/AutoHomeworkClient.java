package com.aquadev.ittopaitelegrambot.client;

import com.aquadev.ittopaitelegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.ittopaitelegrambot.client.dto.JournalSpecResponse;
import com.aquadev.ittopaitelegrambot.client.dto.UpdateAutoHomeworkSettingsRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class AutoHomeworkClient extends BackendClient {

    private static final String SETTINGS_URI = "/api/v1/telegram/auto-homework/settings";
    private static final String GROUP_SPECS_URI = "/api/v1/telegram/journal/group-specs";

    public AutoHomeworkClient(RestClient restClient) {
        super(restClient);
    }

    public AutoHomeworkSettingsResponse getSettings(long telegramUserId) {
        return get(telegramUserId, SETTINGS_URI, AutoHomeworkSettingsResponse.class);
    }

    public AutoHomeworkSettingsResponse updateSettings(long telegramUserId, UpdateAutoHomeworkSettingsRequest request) {
        return put(telegramUserId, SETTINGS_URI, request, AutoHomeworkSettingsResponse.class);
    }

    public List<JournalSpecResponse> getGroupSpecs(long telegramUserId) {
        return get(telegramUserId, GROUP_SPECS_URI, new ParameterizedTypeReference<>() {
        });
    }
}
