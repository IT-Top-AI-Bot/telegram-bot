package com.aquadev.ittopaitelegrambot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updates.GetWebhookInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@Profile("kubernetes")
@RequiredArgsConstructor
public class TelegramConnectionKeepAlive {

    private final TelegramClient telegramClient;

    @Scheduled(fixedDelay = 180_000)
    public void keepAlive() {
        try {
            telegramClient.execute(new GetWebhookInfo());
        } catch (TelegramApiException e) {
            log.warn("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
