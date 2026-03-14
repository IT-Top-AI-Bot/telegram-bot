package com.aquadev.ittopaitelegrambot.bot;

import com.aquadev.ittopaitelegrambot.bot.dispatcher.UpdateDispatcher;
import com.aquadev.ittopaitelegrambot.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Profile("!kubernetes")
@RequiredArgsConstructor
public class LongPollingTelegramBot implements SpringLongPollingBot, LongPollingUpdateConsumer {

    private final TelegramProperties telegramProperties;
    private final UpdateDispatcher updateDispatcher;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public String getBotToken() {
        return telegramProperties.token();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(update -> executor.submit(() -> updateDispatcher.dispatch(update)));
    }
}
