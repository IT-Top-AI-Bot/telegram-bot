package com.aquadev.telegrambot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotCommandsRegistrar {

    private final TelegramClient telegramClient;
    private final CommandRegistry commandRegistry;

    public void run() {
        List<BotCommand> commands = commandRegistry.getCommandMetadata().stream()
                .map(annotation -> (BotCommand) BotCommand.builder()
                        .command(annotation.value().substring(1))
                        .description(annotation.description())
                        .build())
                .toList();

        if (commands.isEmpty()) {
            return;
        }

        try {
            telegramClient.execute(SetMyCommands.builder().commands(commands).build());
            log.info("Registered {} bot command(s): {}",
                    commands.size(),
                    commands.stream().map(BotCommand::getCommand).toList());
        } catch (TelegramApiException e) {
            log.error("Failed to register bot commands: {}", e.getMessage());
        }
    }
}
