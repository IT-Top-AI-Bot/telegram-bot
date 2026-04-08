package com.aquadev.telegrambot.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("admin")
public record AdminProperties(
        List<Long> telegramIds,
        List<ServiceConfig> services
) {

    public record ServiceConfig(String name, String actuatorUrl) {
    }

    public boolean isAdmin(long telegramId) {
        return telegramIds != null && telegramIds.contains(telegramId);
    }
}
