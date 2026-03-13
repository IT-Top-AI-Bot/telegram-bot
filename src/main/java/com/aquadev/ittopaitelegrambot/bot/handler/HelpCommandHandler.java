package com.aquadev.ittopaitelegrambot.bot.handler;

import com.aquadev.ittopaitelegrambot.bot.annotation.TelegramBotCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
@TelegramBotCommand(value = "/help", description = "Показать список команд")
public class HelpCommandHandler implements CommandHandler {

    private final TelegramClient telegramClient;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        String helpText = """
                Доступные команды:
                
                /start — войти в систему или создать аккаунт
                /help  — показать это сообщение
                """;

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(helpText)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Не удалось отправить ответ на /help", e);
        }
    }
}
