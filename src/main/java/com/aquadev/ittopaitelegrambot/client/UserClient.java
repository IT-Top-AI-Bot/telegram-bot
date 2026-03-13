package com.aquadev.ittopaitelegrambot.client;

import com.aquadev.ittopaitelegrambot.client.dto.RegisterRequest;
import com.aquadev.ittopaitelegrambot.client.dto.UserResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class UserClient extends BackendClient {

    private static final String USERS_ME_URI = "/api/v1/telegram/users/me";
    private static final String USERS_URI = "/api/v1/telegram/users";

    public UserClient(RestClient restClient) {
        super(restClient);
    }

    public Optional<UserResponse> getMe(long telegramUserId) {
        try {
            return Optional.ofNullable(get(telegramUserId, USERS_ME_URI, UserResponse.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public UserResponse register(long telegramUserId, String journalUsername, String journalPassword) {
        return post(telegramUserId, USERS_URI, new RegisterRequest(journalUsername, journalPassword), UserResponse.class);
    }
}
