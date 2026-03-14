package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@Profile("kubernetes") // Активируется только в k8s среде
@RequiredArgsConstructor
public class WebhookTelegramBotConfig {

    private final TelegramProperties telegramProperties;
    private final UpdateDispatcher updateDispatcher;

    private final TelegramClient telegramClient;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Bean
    public SpringTelegramWebhookBot springTelegramWebhookBot() {
        return SpringTelegramWebhookBot.builder()
                .botPath(telegramProperties.webhookPath())
                .updateHandler(this::handleUpdate)
                .setWebhook(this::registerWebhook)
                .deleteWebhook(this::removeWebhook)
                .build();
    }


    private BotApiMethod<?> handleUpdate(Update update) {
        executor.submit(() -> updateDispatcher.dispatch(update));

        return null;
    }

    private void registerWebhook() {
        try {
            SetWebhook setWebhook = SetWebhook.builder()
                    .url(telegramProperties.webhookBaseUrl())
                    .build();
            telegramClient.execute(setWebhook);
            log.info("Webhook successfully set to: {}", telegramProperties.webhookBaseUrl());
        } catch (TelegramApiException e) {
            log.error("Failed to set webhook", e);
            throw new RuntimeException(e);
        }
    }


    private void removeWebhook() {
        try {
            DeleteWebhook deleteWebhook = DeleteWebhook.builder()
                    .dropPendingUpdates(false) // false, чтобы не потерять сообщения при рестарте пода
                    .build();
            telegramClient.execute(deleteWebhook);
            log.info("Webhook successfully deleted");
        } catch (TelegramApiException e) {
            log.error("Failed to delete webhook", e);
        }
    }
}
