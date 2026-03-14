package com.aquadev.ittopaitelegrambot.config;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
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
public class TelegramWebhookConfig implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    private final TelegramClient telegramClient;
    private final TelegramProperties telegramProperties;
    private final UpdateDispatcher updateDispatcher;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void configureMessageConverters(java.util.List<HttpMessageConverter<?>> converters) {
        converters.add(0, telegramUpdateConverter());
    }

    @Bean
    public SpringTelegramWebhookBot webhookBot() {
        return SpringTelegramWebhookBot.builder()
                .botPath("callback")
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
                .setWebhook(null)    // registered after server starts — see webhookRegistrar()
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

    @Bean
    public ApplicationRunner webhookRegistrar() {
        return args -> {
            String webhookUrl = telegramProperties.webhookBaseUrl() + "/callback";
            try {
                telegramClient.execute(SetWebhook.builder().url(webhookUrl).build());
                log.info("Webhook registered: {}", webhookUrl);
            } catch (TelegramApiException e) {
                log.error("Failed to register webhook at {}: {}", webhookUrl, e.getMessage());
            }
        };
    }

    /**
     * Jackson 2.x converter for telegrambots Update deserialization.
     * Spring Boot 4.x uses Jackson 3.x (tools.jackson) which is incompatible
     * with telegrambots 9.x Jackson 2.x annotations on model classes.
     */
    @Bean
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
