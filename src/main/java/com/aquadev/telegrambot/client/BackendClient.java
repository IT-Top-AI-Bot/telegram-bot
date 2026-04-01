package com.aquadev.telegrambot.client;

import org.springframework.core.ParameterizedTypeReference;
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

    protected <T> T get(long telegramUserId, String uri, ParameterizedTypeReference<T> responseType) {
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

    protected <T> T put(long telegramUserId, String uri, Object requestBody, Class<T> responseType) {
        try {
            return ScopedValue.where(TelegramUserContext.TG_USER_ID, telegramUserId)
                    .call(() -> restClient.put()
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

    protected void delete(long telegramUserId, String uri) {
        try {
            ScopedValue.where(TelegramUserContext.TG_USER_ID, telegramUserId)
                    .run(() -> restClient.delete()
                            .uri(uri)
                            .retrieve()
                            .toBodilessEntity());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
