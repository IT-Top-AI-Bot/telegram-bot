package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@Profile("kubernetes")
@RequiredArgsConstructor
public class WebhookTelegramBotConfig {

    private final TelegramProperties telegramProperties;
    private final UpdateDispatcher updateDispatcher;
    private final TelegramClient telegramClient;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Bean
    public SpringTelegramWebhookBot springTelegramWebhookBot() {
        // Указываем botPath как "bot". 
        // Префикс будет взят из пути в свойстве telegram.webhook-url. 
        // Если telegram.webhook-url = "https://.../callback/", то путь будет /callback/ + bot = /callback/bot
        String path = telegramProperties.webhookPath(); 
        log.info("Configuring SpringTelegramWebhookBot with path: {}", path);

        return SpringTelegramWebhookBot.builder()
                .botPath(path)
                .updateHandler(this::handleUpdate)
                .build();
    }

    @Bean
    public CommandLineRunner registerWebhookOnStartup() {
        return args -> {
            try {
                // Регистрируем полный URL в Telegram
                String webhookUrl = telegramProperties.webhookBaseUrl();
                log.info("Manually registering webhook URL in Telegram: {}", webhookUrl);

                SetWebhook setWebhook = SetWebhook.builder()
                        .url(webhookUrl)
                        .build();
                telegramClient.execute(setWebhook);
                log.info("Webhook successfully registered!");
            } catch (TelegramApiException e) {
                log.error("Failed to register webhook on startup", e);
            }
        };
    }

    private BotApiMethod<?> handleUpdate(Update update) {
        log.info("Received Update from Telegram: {}", update.getUpdateId());

        executor.submit(() -> {
            try {
                updateDispatcher.dispatch(update);
                log.info("Update dispatched successfully");
            } catch (Exception e) {
                log.error("Error while dispatching update", e);
            }
        });

        return null;
    }
}
