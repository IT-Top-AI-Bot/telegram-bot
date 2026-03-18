package com.aquadev.ittopaitelegrambot.client;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class BackendLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Set<String> LOGGABLE_HEADERS = Set.of(
            "content-type", "accept", "user-agent", "host",
            "x-telegram-user-id", "x-request-id", "x-b3-traceid"
    );

    @Override
    public @NonNull ClientHttpResponse intercept(HttpRequest request, byte @NonNull [] body, ClientHttpRequestExecution execution)
            throws IOException {
        long start = System.currentTimeMillis();
        Long userIdRaw = TelegramUserContext.get();
        String userId = userIdRaw != null ? String.valueOf(userIdRaw) : "system";

        HttpHeaders sanitizedHeaders = new HttpHeaders();
        request.getHeaders().forEach((headerName, headerValues) -> {
            if (LOGGABLE_HEADERS.contains(headerName.toLowerCase())) {
                sanitizedHeaders.addAll(headerName, headerValues);
            } else {
                sanitizedHeaders.add(headerName, "***");
            }
        });

        HttpRequest sanitizedRequest = new HttpRequestWrapper(request) {
            @Override
            public @NonNull HttpHeaders getHeaders() {
                return sanitizedHeaders;
            }
        };

        log.info("→ {} {} [user={}] | Headers: {}",
                sanitizedRequest.getMethod(), sanitizedRequest.getURI(), userId, sanitizedRequest.getHeaders());

        ClientHttpResponse response = execution.execute(request, body);

        long duration = System.currentTimeMillis() - start;
        HttpStatusCode status = response.getStatusCode();
        String logLine = "← {} {} [user={}] in {}ms";

        if (status.is2xxSuccessful()) {
            log.info(logLine, status.value(), request.getURI(), userId, duration);
        } else if (status.is4xxClientError()) {
            log.warn(logLine, status.value(), request.getURI(), userId, duration);
        } else {
            log.error(logLine, status.value(), request.getURI(), userId, duration);
        }

        return response;
    }
}
