package com.aquadev.telegrambot.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("telegram.bot")
public record TelegramProperties(
        String token,
        String username,
        Webhook webhook
) {
    public record Webhook(String host, String path, String secretToken) {
        public String url() {
            return host + "/" + path;
        }
    }
}
