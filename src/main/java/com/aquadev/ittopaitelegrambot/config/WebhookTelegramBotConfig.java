package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Configuration
@Profile("kubernetes")
@RequiredArgsConstructor
public class WebhookTelegramBotConfig {

    private final TelegramProperties telegramProperties;
    private final TelegramClient telegramClient;

    @Bean
    public CommandLineRunner registerWebhookOnStartup() {
        return args -> {
            try {
                String webhookUrl = telegramProperties.webhookBaseUrl();
                log.info("Registering webhook URL in Telegram: {}", webhookUrl);
                telegramClient.execute(SetWebhook.builder().url(webhookUrl).build());
                log.info("Webhook successfully registered!");
            } catch (TelegramApiException e) {
                log.error("Failed to register webhook on startup", e);
            }
        };
    }
}
