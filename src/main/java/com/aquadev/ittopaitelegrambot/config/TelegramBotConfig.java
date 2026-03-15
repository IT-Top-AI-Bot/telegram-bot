package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {

    private final TelegramProperties telegramProperties;

    @Bean
    public TelegramClient telegramClient() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(20);
        dispatcher.setMaxRequests(40);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                // keepAlive < Kubernetes NAT idle timeout (~60-90s) to avoid stale connections
                .connectionPool(new ConnectionPool(20, 55, TimeUnit.SECONDS))
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        return new OkHttpTelegramClient(httpClient, telegramProperties.token());
    }
}
