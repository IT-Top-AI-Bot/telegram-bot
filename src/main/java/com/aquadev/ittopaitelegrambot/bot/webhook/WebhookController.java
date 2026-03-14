package com.aquadev.ittopaitelegrambot.bot.webhook;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@Profile("kubernetes")
@RequiredArgsConstructor
public class WebhookController {

    private final UpdateDispatcher updateDispatcher;
    private final TelegramProperties telegramProperties;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @PostMapping("/webhook/{token}")
    public ResponseEntity<Void> onUpdate(@PathVariable String token, @RequestBody Update update) {
        if (!telegramProperties.token().equals(token)) {
            return ResponseEntity.status(403).build();
        }
        executor.submit(() -> updateDispatcher.dispatch(update));
        return ResponseEntity.ok().build();
    }
}
