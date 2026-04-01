package com.aquadev.telegrambot.config;

import com.aquadev.telegrambot.bot.BotCommandsRegistrar;
import com.aquadev.telegrambot.bot.dispatcher.UpdateDispatcher;
import com.aquadev.telegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;

@Slf4j
@Configuration
@Profile("kubernetes")
@RequiredArgsConstructor
@ImportRuntimeHints(TelegramRuntimeHints.class)
public class WebhookTelegramBotConfig {

    private final TelegramClient telegramClient;
    private final UpdateDispatcher updateDispatcher;
    private final TelegramProperties telegramProperties;
    private final BotCommandsRegistrar botCommandsRegistrar;

    @Bean
    public SpringTelegramWebhookBot telegramWebhookBot() {
        return SpringTelegramWebhookBot.builder()
                .botPath(telegramProperties.webhook().path())
                .updateHandler(update -> {
                    updateDispatcher.dispatch(update);
                    return null;
                })
                .setWebhook(() -> {
                })
                .deleteWebhook(() -> log.info("Skipping webhook deletion to preserve webhook during rolling updates"))
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerWebhook() {
        try {
            boolean success = telegramClient.execute(
                    SetWebhook.builder()
                            .url(telegramProperties.webhook().url())
                            .secretToken(telegramProperties.webhook().secretToken())
                            .build()
            );
            log.info("SetWebhook execution status: {}", success);
            log.info("Webhook registered: {}", telegramProperties.webhook().url());
            botCommandsRegistrar.run();
        } catch (TelegramApiException e) {
            log.error("CRITICAL: Failed to register webhook on startup", e);
        }
    }
}
