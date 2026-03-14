package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.client.BackendLoggingInterceptor;
import com.aquadev.ittopaitelegrambot.client.TelegramUserIdInterceptor;
import com.aquadev.ittopaitelegrambot.config.properties.BackendProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final BackendProperties backendProperties;
    private final TelegramUserIdInterceptor telegramUserIdInterceptor;
    private final BackendLoggingInterceptor backendLoggingInterceptor;

    @Bean
    public RestClient restClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        return RestClient.builder()
                .baseUrl(backendProperties.baseUrl())
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .requestInterceptor(telegramUserIdInterceptor)
                .requestInterceptor(backendLoggingInterceptor)
                .build();
    }
}
