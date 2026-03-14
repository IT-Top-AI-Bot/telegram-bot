package com.aquadev.ittopaitelegrambot.bot.webhook;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@Profile("kubernetes")
public class WebhookController {

    private final UpdateDispatcher updateDispatcher;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    // Jackson 2.x ObjectMapper — telegrambots использует com.fasterxml.jackson,
    // а Spring Boot 4.0 по умолчанию использует tools.jackson (3.x), которые несовместимы.
    private final ObjectMapper telegramObjectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    public WebhookController(UpdateDispatcher updateDispatcher) {
        this.updateDispatcher = updateDispatcher;
    }

    @PostMapping(value = "/callback/bot", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> onUpdate(@RequestBody String body) {
        try {
            Update update = telegramObjectMapper.readValue(body, Update.class);
            log.info("Received update id={}", update.getUpdateId());
            executor.submit(() -> {
                try {
                    updateDispatcher.dispatch(update);
                } catch (Exception e) {
                    log.error("Error dispatching update", e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to deserialize update: {}", body, e);
        }
        return ResponseEntity.ok().build();
    }
}
