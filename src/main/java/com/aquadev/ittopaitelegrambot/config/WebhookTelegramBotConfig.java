package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final TelegramProperties telegramProperties;
    private final TelegramClient telegramClient;
    private final UpdateDispatcher updateDispatcher;

    @Bean
    public SpringTelegramWebhookBot webhookBot() {
        return SpringTelegramWebhookBot.builder()
                .botPath(telegramProperties.webhookPath())
                .updateHandler(update -> {
                    updateDispatcher.dispatch(update);
                    return null;
                })
                .setWebhook(() -> {
                    try {
                        String webhookUrl = telegramProperties.webhookBaseUrl();
                        log.info("Registering webhook URL in Telegram: {}", webhookUrl);
                        telegramClient.execute(SetWebhook.builder()
                                .url(webhookUrl)
                                .maxConnections(100)
                                .allowedUpdates(List.of("message", "callback_query"))
                                .build());
                        log.info("Webhook successfully registered!");
                    } catch (TelegramApiException e) {
                        log.error("Failed to register webhook on startup", e);
                    }
                })
                .deleteWebhook(() -> {
                    try {
                        telegramClient.execute(new DeleteWebhook());
                        log.info("Webhook deleted");
                    } catch (TelegramApiException e) {
                        log.error("Failed to delete webhook on shutdown", e);
                    }
                })
                .build();
    }
}
