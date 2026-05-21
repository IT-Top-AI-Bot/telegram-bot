package com.aquadev.telegrambot.config;

import com.aquadev.telegrambot.config.properties.ProxyProperties;
import com.aquadev.telegrambot.config.properties.TelegramProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
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
        HttpLoggingInterceptor logging =
                new HttpLoggingInterceptor(log::info);

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS));

        if (proxyProperties.isEnabled()) {
            log.info("Proxy enabled: {}:{}", proxyProperties.host(), proxyProperties.port());
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

            return TelegramUrl.builder()
                    .schema(uri.getScheme())
                    .host(uri.getHost())
                    .port(uri.getPort() == -1 ? (uri.getScheme().equals("https") ? 443 : 80) : uri.getPort())
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
