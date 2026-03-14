package com.aquadev.ittopaitelegrambot.bot.webhook;

import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@Profile("kubernetes")
@RequiredArgsConstructor
public class WebhookRegistrar implements ApplicationRunner, DisposableBean {

    private final TelegramClient telegramClient;
    private final TelegramProperties telegramProperties;

    @Override
    public void run(ApplicationArguments args) throws TelegramApiException {
        String webhookUrl = telegramProperties.webhookBaseUrl()
                + "/webhook/" + telegramProperties.token();

        telegramClient.execute(SetWebhook.builder()
                .url(webhookUrl)
                .build());

        log.info("Webhook registered: {}", webhookUrl);
    }

    @Override
    public void destroy() {
        try {
            telegramClient.execute(DeleteWebhook.builder().dropPendingUpdates(true).build());
            log.info("Webhook deleted");
        } catch (TelegramApiException e) {
            log.error("Failed to delete webhook on shutdown", e);
        }
    }
}
