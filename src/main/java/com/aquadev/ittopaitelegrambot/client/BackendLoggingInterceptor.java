package com.aquadev.ittopaitelegrambot.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class BackendLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        long start = System.currentTimeMillis();
        String userId = request.getHeaders().getFirst("X-Telegram-User-Id");

        log.info("→ {} {} [user={}] | Headers: {}",
                request.getMethod(), request.getURI(), userId, request.getHeaders());

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
