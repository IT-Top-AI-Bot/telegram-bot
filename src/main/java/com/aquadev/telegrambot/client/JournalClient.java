package com.aquadev.telegrambot.client;

import com.aquadev.telegrambot.client.dto.JournalProfileResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class JournalClient extends BackendClient {

    private static final String JOURNAL_ME_URI = "/api/v1/telegram/journal/me";

    public JournalClient(RestClient restClient) {
        super(restClient);
    }

    public JournalProfileResponse getProfile(long telegramUserId) {
        return get(telegramUserId, JOURNAL_ME_URI, JournalProfileResponse.class);
    }
}
