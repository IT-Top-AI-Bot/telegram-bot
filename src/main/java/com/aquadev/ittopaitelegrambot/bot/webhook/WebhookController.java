package com.aquadev.ittopaitelegrambot.bot.webhook;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@Profile("kubernetes")
@RequiredArgsConstructor
public class WebhookController {

    private final UpdateDispatcher updateDispatcher;
    private final TelegramProperties telegramProperties;
    private final ObjectMapper telegramObjectMapper = new ObjectMapper();

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @PostMapping("/webhook/{token}")
    public ResponseEntity<Void> onUpdate(@PathVariable String token, @RequestBody byte[] body) {
        if (!telegramProperties.token().equals(token)) {
            log.warn("Webhook called with invalid token");
            return ResponseEntity.status(403).build();
        }
        String json = new String(body, java.nio.charset.StandardCharsets.UTF_8);
        log.debug("Webhook update received: {}", json);
        executor.submit(() -> {
            try {
                Update update = telegramObjectMapper.readValue(json, Update.class);
                updateDispatcher.dispatch(update);
            } catch (Exception e) {
                log.error("Failed to process webhook update", e);
            }
        });
        return ResponseEntity.ok().build();
    }
}
