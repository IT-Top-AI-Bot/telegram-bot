package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {

    private final TelegramProperties telegramProperties;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(telegramProperties.token());
    }
}
