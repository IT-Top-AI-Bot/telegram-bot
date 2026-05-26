package com.aquadev.telegrambot.client;

import com.aquadev.telegrambot.client.dto.JournalFutureExamResponse;
import com.aquadev.telegrambot.client.dto.JournalProfileResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class JournalClient extends BackendClient {

    private static final String JOURNAL_ME_URI = "/api/v1/telegram/journal/me";
    private static final String JOURNAL_FUTURE_EXAMS = "/api/v1/telegram/journal/future-exams";

    public JournalClient(RestClient restClient) {
        super(restClient);
    }

    public JournalProfileResponse getProfile(long telegramUserId) {
        return get(telegramUserId, JOURNAL_ME_URI, JournalProfileResponse.class);
    }

    public List<JournalFutureExamResponse> getFutureExams(long telegramUserId) {
        return get(telegramUserId, JOURNAL_FUTURE_EXAMS, new ParameterizedTypeReference<>() {
        });
    }
}
