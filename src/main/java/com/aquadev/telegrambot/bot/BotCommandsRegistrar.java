package com.aquadev.telegrambot.bot;

import com.aquadev.telegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.telegrambot.client.UserRole;
import com.aquadev.telegrambot.config.properties.AdminProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotCommandsRegistrar {

    private final TelegramClient telegramClient;
    private final CommandRegistry commandRegistry;
    private final AdminProperties adminProperties;

    public void run() {
        List<BotCommand> commands = commandRegistry.getCommandMetadata().stream()
                .filter(this::isPublicCommand)
                .map(annotation -> (BotCommand) BotCommand.builder()
                        .command(annotation.value().substring(1))
                        .description(annotation.description())
                        .build())
                .toList();

        List<BotCommand> allCommands = commandRegistry.getCommandMetadata().stream()
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
            for (long telegramId : adminProperties.telegramIds()) {
                try {
                    BotCommandScopeChat scope = BotCommandScopeChat.builder().chatId(telegramId).build();
                    telegramClient.execute(SetMyCommands.builder().scope(scope).commands(allCommands).build());
                } catch (TelegramApiException e) {
                    log.warn("Failed to register command for admin user: {}", telegramId, e);
                }
            }
            log.info("Registered {} bot command(s): {}",
                    commands.size(),
                    commands.stream().map(BotCommand::getCommand).toList());
        } catch (TelegramApiException e) {
            log.error("Failed to register bot commands: {}", e.getMessage());
        }
    }

    private boolean isPublicCommand(TelegramBotCommand annotation) {
        return Arrays.asList(annotation.roles()).contains(UserRole.USER);
    }
}
