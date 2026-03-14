package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
        // The default prefix for the starter is /callback, so the full URL must be /callback/{botPath}
        String webhookUrl = telegramProperties.webhookBaseUrl() + "/callback/" + telegramProperties.token();
        return SetWebhook.builder()
                .url(webhookUrl)
                .dropPendingUpdates(true)
                .build();
    }

    @Bean
    public SpringTelegramWebhookBot webhookBot(SetWebhook setWebhook) {
        return SpringTelegramWebhookBot.builder()
                .botPath(telegramProperties.token())
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
                .setWebhook(setWebhook)
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
     * Jackson 2.x converter for telegrambots Update deserialization.
     * Spring Boot 4.x uses Jackson 3.x (tools.jackson) which is incompatible
     * with telegrambots 9.x Jackson 2.x annotations on model classes.
     */
    @Bean
    @org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
    public HttpMessageConverter<Update> telegramUpdateConverter() {
        return new AbstractHttpMessageConverter<>(MediaType.APPLICATION_JSON) {
            private final ObjectMapper mapper = new ObjectMapper();

            @Override
            protected boolean supports(Class<?> clazz) {
                return Update.class.isAssignableFrom(clazz);
            }

            @Override
            protected Update readInternal(Class<? extends Update> clazz, HttpInputMessage inputMessage)
                    throws IOException {
                log.info("Converting incoming update using telegramUpdateConverter...");
                try {
                    Update update = mapper.readValue(inputMessage.getBody(), clazz);
                    log.info("Successfully converted update: {}", update.getUpdateId());
                    return update;
                } catch (Exception e) {
                    log.error("Failed to convert update: {}", e.getMessage(), e);
                    throw e;
                }
            }

            @Override
            protected void writeInternal(Update update, HttpOutputMessage outputMessage) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
