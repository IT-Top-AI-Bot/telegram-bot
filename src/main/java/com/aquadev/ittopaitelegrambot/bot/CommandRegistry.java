package com.aquadev.ittopaitelegrambot.bot;

import com.aquadev.ittopaitelegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.ittopaitelegrambot.bot.handler.CommandHandler;
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

    private Map<String, CommandHandler> commandMap;

    /**
     * Метаданные команд в порядке регистрации (для /help, SetMyCommands и т.д.)
     */
    @Getter
    private List<TelegramBotCommand> commandMetadata;

    @PostConstruct
    void build() {
        Map<String, CommandHandler> map = new HashMap<>();
        List<TelegramBotCommand> metadata = new ArrayList<>();

        for (CommandHandler handler : handlers) {
            TelegramBotCommand annotation = AopUtils.getTargetClass(handler).getAnnotation(TelegramBotCommand.class);
            if (annotation != null) {
                String command = annotation.value();
                if (map.containsKey(command)) {
                    CommandHandler existing = map.get(command);
                    throw new IllegalStateException("Duplicate command found: '" + command + "' in handlers "
                            + AopUtils.getTargetClass(existing).getName() + " and "
                            + AopUtils.getTargetClass(handler).getName());
                }
                map.put(command, handler);
                metadata.add(annotation);
            }
        }

        commandMap = Collections.unmodifiableMap(map);
        commandMetadata = Collections.unmodifiableList(metadata);
    }

    public CommandHandler find(String command) {
        return commandMap.get(command);
    }
}
