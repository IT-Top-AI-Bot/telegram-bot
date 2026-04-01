package com.aquadev.telegrambot.bot.dispatcher;

import com.aquadev.telegrambot.bot.callback.CallbackHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackDispatcher {

    private final List<CallbackHandler> handlers;

    public void dispatch(Update update) {
        String data = update.getCallbackQuery().getData();
        if (data == null) {
            log.debug("Callback без данных, пропускаем");
            return;
        }

        handlers.stream()
                .filter(h -> h.supports(data))
                .findFirst()
                .ifPresentOrElse(
                        h -> {
                            log.info("Dispatching callback '{}' → {}", data, h.getClass().getSimpleName());
                            h.handle(update);
                        },
                        () -> log.warn("Нет обработчика для callback: {}", data)
                );
    }
}
