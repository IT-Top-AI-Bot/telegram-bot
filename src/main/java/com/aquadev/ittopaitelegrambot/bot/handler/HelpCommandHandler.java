package com.aquadev.ittopaitelegrambot.bot.handler;

import com.aquadev.ittopaitelegrambot.bot.CommandRegistry;
import com.aquadev.ittopaitelegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.stream.Collectors;

@Component
@TelegramBotCommand(value = "/help", description = "Показать список команд")
public class HelpCommandHandler implements CommandHandler {

    private final TelegramMessageSender sender;
    private final CommandRegistry commandRegistry;

    public HelpCommandHandler(TelegramMessageSender sender, @Lazy CommandRegistry commandRegistry) {
        this.sender = sender;
        this.commandRegistry = commandRegistry;
    }

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();

        String commandList = commandRegistry.getCommandMetadata().stream()
                .map(cmd -> "%s — %s".formatted(cmd.value(), cmd.description()))
                .collect(Collectors.joining("\n"));

        sender.send(chatId, "Доступные команды:\n\n" + commandList);
    }
}
