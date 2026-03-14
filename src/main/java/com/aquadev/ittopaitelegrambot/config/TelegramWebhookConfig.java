package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@Profile("kubernetes")
@RequiredArgsConstructor
public class TelegramWebhookConfig {

    private final TelegramClient telegramClient;
    private final TelegramProperties telegramProperties;
    private final UpdateDispatcher updateDispatcher;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Bean
    public SetWebhook setWebhook() {
        // Starter uses /callback/{botPath}
        String webhookUrl = telegramProperties.webhookBaseUrl() + "/callback/tg";
        return SetWebhook.builder()
                .url(webhookUrl)
                .dropPendingUpdates(true)
                .build();
    }

    @Bean
    public SpringTelegramWebhookBot webhookBot(SetWebhook setWebhook) {
        return SpringTelegramWebhookBot.builder()
                .botPath("tg") // Avoid colon in path
                .updateHandler(update -> {
                    executor.submit(() -> {
                        try {
                            updateDispatcher.dispatch(update);
                        } catch (Exception e) {
                            log.error("Error dispatching update: {}", e.getMessage(), e);
                        }
                    });
                    return null;
                })
                .setWebhook(() -> {
                    try {
                        telegramClient.execute(setWebhook);
                        log.info("Webhook registered: {}", setWebhook.getUrl());
                    } catch (TelegramApiException e) {
                        log.error("Failed to register webhook at {}: {}", setWebhook.getUrl(), e.getMessage());
                    }
                })
                .deleteWebhook(() -> {
                    try {
                        telegramClient.execute(DeleteWebhook.builder().dropPendingUpdates(true).build());
                        log.info("Webhook deleted");
                    } catch (TelegramApiException e) {
                        log.error("Failed to delete webhook", e);
                    }
                })
                .build();
    }

    /**
     * Custom converter for Telegram Update objects using Jackson 2.x.
     * We extend AbstractHttpMessageConverter directly to avoid using the deprecated
     * MappingJackson2HttpMessageConverter in Spring 7.0 / Boot 4.0.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public HttpMessageConverter<Update> telegramUpdateConverter() {
        return new AbstractHttpMessageConverter<>(MediaType.APPLICATION_JSON) {
            private final ObjectMapper mapper = new ObjectMapper();

            @Override
            protected boolean supports(@NonNull Class<?> clazz) {
                return Update.class.isAssignableFrom(clazz);
            }

            @Override
            protected Update readInternal(@NonNull Class<? extends Update> clazz, @NonNull HttpInputMessage inputMessage)
                    throws IOException {
                return mapper.readValue(inputMessage.getBody(), clazz);
            }

            @Override
            protected void writeInternal(Update update, @NonNull HttpOutputMessage outputMessage) {
                throw new UnsupportedOperationException("Write is not supported");
            }
        };
    }
}
