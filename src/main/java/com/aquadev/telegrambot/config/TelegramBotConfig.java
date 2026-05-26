package com.aquadev.telegrambot.config;

import com.aquadev.telegrambot.config.properties.ProxyProperties;
import com.aquadev.telegrambot.config.properties.TelegramProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {

    private final TelegramProperties telegramProperties;
    private final ProxyProperties proxyProperties;

    @Bean
    public OkHttpClient okHttpClient() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(65, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS));

        if (proxyProperties.isEnabled()) {
            builder.proxy(new Proxy(
                    Proxy.Type.SOCKS,
                    new InetSocketAddress(
                            proxyProperties.host(),
                            proxyProperties.port()
                    )
            ));
        }

        return builder.build();
    }

    @Bean
    public TelegramUrl telegramUrl() {
        if (telegramProperties.apiUrl() != null
                && !telegramProperties.apiUrl().isBlank()) {

            URI uri = URI.create(telegramProperties.apiUrl());
            int port = uri.getPort();

            if (port == -1) {
                if ("https".equals(uri.getScheme())) {
                    port = 443;
                } else {
                    port = 80;
                }
            }

            return TelegramUrl.builder()
                    .schema(uri.getScheme())
                    .host(uri.getHost())
                    .port(port)
                    .build();
        }
        return TelegramUrl.DEFAULT_URL;
    }

    @Bean
    public TelegramClient telegramClient(OkHttpClient okHttpClient, TelegramUrl telegramUrl) {
        return new OkHttpTelegramClient(
                okHttpClient,
                telegramProperties.token(),
                telegramUrl
        );
    }

    @Bean(destroyMethod = "close")
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication(
            ObjectProvider<ObjectMapper> objectMapperProvider,
            OkHttpClient okHttpClient
    ) {
        return new TelegramBotsLongPollingApplication(
                () -> objectMapperProvider.getIfAvailable(ObjectMapper::new),
                () -> okHttpClient
        );
    }
}
