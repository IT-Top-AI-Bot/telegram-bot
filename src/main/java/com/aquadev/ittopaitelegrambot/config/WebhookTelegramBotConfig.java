package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;

import java.util.List;

@Slf4j
@Configuration
@Profile("kubernetes")
@RequiredArgsConstructor
@ImportRuntimeHints(TelegramRuntimeHints.class)
public class WebhookTelegramBotConfig {

    private final TelegramClient telegramClient;
    private final UpdateDispatcher updateDispatcher;

    @Value("${telegram.bot.webhook-path}")
    private String webhookPath;

    @Value("${telegram.bot.webhook-base-url}")
    private String webhookBaseUrl;

    @Bean
    public SpringTelegramWebhookBot telegramWebhookBot() {
        return SpringTelegramWebhookBot.builder()
                .botPath(webhookPath)
                .updateHandler(update -> {
                    updateDispatcher.dispatch(update);
                    return null;
                })
                .setWebhook(this::registerWebhook)
                .deleteWebhook(this::deleteWebhook)
                .build();
    }

    private void registerWebhook() {
        try {
            log.info("Registering webhook URL in Telegram: {}", webhookBaseUrl);
            telegramClient.execute(
                    SetWebhook.builder()
                            .url(webhookBaseUrl)
                            .maxConnections(100)
                            .allowedUpdates(List.of("message", "callback_query"))
                            .build()
            );
            log.info("Webhook successfully registered!");
        } catch (TelegramApiException e) {
            log.error("Failed to register webhook on startup", e);
        }
    }

    private void deleteWebhook() {
        try {
            telegramClient.execute(new DeleteWebhook());
            log.info("Webhook deleted");
        } catch (TelegramApiException e) {
            log.error("Failed to delete webhook on shutdown", e);
        }
    }
}
