package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.client.BackendLoggingInterceptor;
import com.aquadev.ittopaitelegrambot.client.TelegramUserIdInterceptor;
import com.aquadev.ittopaitelegrambot.config.properties.BackendProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final BackendProperties backendProperties;
    private final TelegramUserIdInterceptor telegramUserIdInterceptor;
    private final BackendLoggingInterceptor backendLoggingInterceptor;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(backendProperties.baseUrl())
                .requestInterceptor(telegramUserIdInterceptor)  // сначала добавляет заголовок
                .requestInterceptor(backendLoggingInterceptor)  // затем логирует вместе с ним
                .build();
    }
}
