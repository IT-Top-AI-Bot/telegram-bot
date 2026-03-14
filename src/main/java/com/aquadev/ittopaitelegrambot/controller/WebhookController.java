package com.aquadev.ittopaitelegrambot.controller;

import com.aquadev.ittopaitelegrambot.config.WebhookTelegramBotConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RestController
@Profile("kubernetes")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookTelegramBotConfig botConfig;

    @PostMapping("/callback/bot")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        log.info("Webhook endpoint hit: {}", update.getUpdateId());
        return botConfig.handleUpdate(update);
    }
}
