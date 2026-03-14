package com.aquadev.ittopaitelegrambot.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("telegram.bot")
public record TelegramProperties(
        String token,
        String username,
        String webhookBaseUrl
) {
}
