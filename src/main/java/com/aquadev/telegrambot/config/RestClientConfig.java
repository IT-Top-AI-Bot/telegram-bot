package com.aquadev.telegrambot.config;

import com.aquadev.telegrambot.client.BackendLoggingInterceptor;
import com.aquadev.telegrambot.client.TelegramUserIdInterceptor;
import com.aquadev.telegrambot.client.UriClientObservationConvention;
import com.aquadev.telegrambot.config.properties.BackendProperties;
import io.micrometer.observation.ObservationRegistry;
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
    public RestClient restClient(ObservationRegistry observationRegistry) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        return RestClient.builder()
                .baseUrl(backendProperties.baseUrl())
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .requestInterceptor(telegramUserIdInterceptor)
                .requestInterceptor(backendLoggingInterceptor)
                .observationRegistry(observationRegistry)
                .observationConvention(new UriClientObservationConvention())
                .build();
    }
}
