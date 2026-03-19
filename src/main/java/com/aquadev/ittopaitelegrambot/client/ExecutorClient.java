package com.aquadev.ittopaitelegrambot.client;

import com.aquadev.ittopaitelegrambot.client.dto.SubjectPromptDto;
import com.aquadev.ittopaitelegrambot.client.dto.UpsertSubjectPromptRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
public class ExecutorClient extends BackendClient {

    private static final String SUBJECT_PROMPTS_URI = "/api/v1/subject-prompts";

    public ExecutorClient(RestClient restClient) {
        super(restClient);
    }

    public List<SubjectPromptDto> getAllPrompts(long telegramUserId) {
        return get(telegramUserId, SUBJECT_PROMPTS_URI, new ParameterizedTypeReference<>() {
        });
    }

    public Optional<SubjectPromptDto> getPromptBySpecId(long telegramUserId, Long specId) {
        try {
            SubjectPromptDto dto = get(telegramUserId, SUBJECT_PROMPTS_URI + "/" + specId, SubjectPromptDto.class);
            return Optional.ofNullable(dto);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }

    public SubjectPromptDto upsertPrompt(long telegramUserId, Long specId, UpsertSubjectPromptRequest request) {
        return put(telegramUserId, SUBJECT_PROMPTS_URI + "/" + specId, request, SubjectPromptDto.class);
    }

    public void deletePrompt(long telegramUserId, Long specId) {
        delete(telegramUserId, SUBJECT_PROMPTS_URI + "/" + specId);
    }
}
