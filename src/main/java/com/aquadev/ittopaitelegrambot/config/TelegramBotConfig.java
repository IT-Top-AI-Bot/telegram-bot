package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.config.properties.ProxyProperties;
import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {

    private final TelegramProperties telegramProperties;
    private final ProxyProperties proxyProperties;

    @Bean
    public TelegramClient telegramClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (proxyProperties.isEnabled()) {
            builder.proxy(new Proxy(
                    Proxy.Type.SOCKS,
                    new InetSocketAddress(proxyProperties.host(), proxyProperties.port())
            ));
        }

        return new OkHttpTelegramClient(builder.build(), telegramProperties.token());
    }
}
