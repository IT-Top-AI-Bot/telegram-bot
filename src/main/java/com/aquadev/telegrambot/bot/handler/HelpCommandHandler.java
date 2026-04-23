package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.CommandRegistry;
import com.aquadev.telegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.UserRole;
import com.aquadev.telegrambot.config.properties.AdminProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@TelegramBotCommand(value = "/help", description = "Показать список команд", requiresValidCredentials = false)
public class HelpCommandHandler implements CommandHandler {

    private final TelegramMessageSender sender;
    private final ObjectProvider<CommandRegistry> commandRegistry;
    private final AdminProperties adminProperties;

    public HelpCommandHandler(TelegramMessageSender sender, ObjectProvider<CommandRegistry> commandRegistry,
                              AdminProperties adminProperties) {
        this.sender = sender;
        this.commandRegistry = commandRegistry;
        this.adminProperties = adminProperties;
    }

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long telegramUserId = update.getMessage().getFrom().getId();
        boolean isAdmin = adminProperties.isAdmin(telegramUserId);

        String commandList = commandRegistry.getObject().getCommandMetadata().stream()
                .filter(cmd -> isAdmin || !Arrays.asList(cmd.roles()).contains(UserRole.ADMIN))
                .map(cmd -> "<b>%s</b> — %s".formatted(cmd.value(), cmd.description()))
                .collect(Collectors.joining("\n"));

        sender.sendHtml(chatId, "📋 <b>Доступные команды:</b>\n\n" + commandList);
    }
}
