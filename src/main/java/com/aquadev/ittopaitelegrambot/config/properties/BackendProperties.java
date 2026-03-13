package com.aquadev.ittopaitelegrambot.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("backend")
public record BackendProperties(
        String baseUrl
) {
}
