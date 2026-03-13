package com.aquadev.ittopaitelegrambot.client;

import org.springframework.web.client.RestClient;

public abstract class BackendClient {

    private final RestClient restClient;

    protected BackendClient(RestClient restClient) {
        this.restClient = restClient;
    }

    protected <T> T get(long telegramUserId, String uri, Class<T> responseType) {
        try {
            return ScopedValue.where(TelegramUserContext.TG_USER_ID, telegramUserId)
                    .call(() -> restClient.get()
                            .uri(uri)
                            .retrieve()
                            .body(responseType));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T post(long telegramUserId, String uri, Object requestBody, Class<T> responseType) {
        try {
            return ScopedValue.where(TelegramUserContext.TG_USER_ID, telegramUserId)
                    .call(() -> restClient.post()
                            .uri(uri)
                            .body(requestBody)
                            .retrieve()
                            .body(responseType));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
