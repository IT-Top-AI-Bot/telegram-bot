package com.aquadev.telegrambot.bot;

import com.aquadev.telegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.telegrambot.bot.handler.CommandHandler;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommandRegistry {

    private final List<CommandHandler> handlers;

    private Map<String, CommandHandler> commandMap = Map.of();
    private Map<String, TelegramBotCommand> annotationMap = Map.of();

    @Getter
    private List<TelegramBotCommand> commandMetadata = List.of();

    @PostConstruct
    void build() {
        Map<String, CommandHandler> map = new HashMap<>();
        Map<String, TelegramBotCommand> annotations = new HashMap<>();
        List<TelegramBotCommand> metadata = new ArrayList<>();

        for (CommandHandler handler : handlers) {
            TelegramBotCommand annotation = AopUtils.getTargetClass(handler).getAnnotation(TelegramBotCommand.class);
            if (annotation != null) {
                String command = annotation.value();
                CommandHandler existing = map.putIfAbsent(command, handler);

                if (existing != null) {
                    throw new IllegalStateException(String.format(
                            "Duplicate command found: '%s' in handlers %s and %s",
                            command,
                            AopUtils.getTargetClass(existing).getName(),
                            AopUtils.getTargetClass(handler).getName()
                    ));
                }

                annotations.put(command, annotation);
                metadata.add(annotation);
            }
        }

        commandMap = Collections.unmodifiableMap(map);
        annotationMap = Collections.unmodifiableMap(annotations);
        commandMetadata = Collections.unmodifiableList(metadata);
    }

    public CommandHandler find(String command) {
        return commandMap.get(command);
    }

    public TelegramBotCommand getAnnotation(String command) {
        return annotationMap.get(command);
    }
}
