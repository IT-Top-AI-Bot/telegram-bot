package com.aquadev.telegrambot.config;

import com.aquadev.telegrambot.config.properties.ProxyProperties;
import com.aquadev.telegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {

    private final TelegramProperties telegramProperties;
    private final ProxyProperties proxyProperties;

    @Bean
    public TelegramClient telegramClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS));

        if (proxyProperties.isEnabled()) {
            builder.proxy(new Proxy(
                    Proxy.Type.SOCKS,
                    new InetSocketAddress(proxyProperties.host(), proxyProperties.port())
            ));
        }

        return new OkHttpTelegramClient(builder.build(), telegramProperties.token());
    }
}
