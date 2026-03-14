package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
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
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Bean
    public SpringTelegramWebhookBot springTelegramWebhookBot() {
        String path = telegramProperties.webhookPath();

        // Извлекаем только последнюю часть пути, например "bot" из "/callback/bot"
        if (path != null && path.contains("/")) {
            String[] segments = path.split("/");
            path = segments[segments.length - 1];
        }

        log.info("Configuring SpringTelegramWebhookBot with path: {}", path);

        return SpringTelegramWebhookBot.builder()
                .botPath(path)
                .updateHandler(this::handleUpdate)
                .build();
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
