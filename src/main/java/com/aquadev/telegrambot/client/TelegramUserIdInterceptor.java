package com.aquadev.telegrambot.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TelegramUserIdInterceptor implements ClientHttpRequestInterceptor {

    private static final String HEADER = "X-Telegram-User-Id";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        Long telegramUserId = TelegramUserContext.get();
        if (telegramUserId == null) {
            throw new IllegalStateException("X-Telegram-User-Id не установлен для запроса: " + request.getURI());
        }
        request.getHeaders().add(HEADER, String.valueOf(telegramUserId));
        return execution.execute(request, body);
    }
}
