package com.aquadev.telegrambot.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("proxy")
public record ProxyProperties(
        String host,
        int port
) {
    public boolean isEnabled() {
        return host != null && !host.isBlank();
    }
}
