package com.aquadev.ittopaitelegrambot.bot;

import com.aquadev.ittopaitelegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.ittopaitelegrambot.bot.handler.CommandHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotCommandsRegistrar {

    private final TelegramClient telegramClient;
    private final List<CommandHandler> handlers;

    @PostConstruct
    void registerCommands() {
        List<BotCommand> commands = new ArrayList<>();

        for (CommandHandler handler : handlers) {
            TelegramBotCommand annotation = AopUtils.getTargetClass(handler).getAnnotation(TelegramBotCommand.class);
            if (annotation != null) {
                commands.add(BotCommand.builder()
                        .command(annotation.value().substring(1))
                        .description(annotation.description())
                        .build());
            }
        }

        if (commands.isEmpty()) {
            return;
        }

        try {
            telegramClient.execute(SetMyCommands.builder().commands(commands).build());
            log.info("Registered {} bot command(s): {}",
                    commands.size(),
                    commands.stream().map(BotCommand::getCommand).toList());
        } catch (TelegramApiException e) {
            log.error("Failed to register bot commands", e);
        }
    }
}
