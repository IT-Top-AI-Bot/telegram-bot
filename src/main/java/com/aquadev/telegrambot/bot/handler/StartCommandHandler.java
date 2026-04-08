package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.RegistrationStateService;
import com.aquadev.telegrambot.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@TelegramBotCommand(value = "/start", description = "Запустить бота")
public class StartCommandHandler implements CommandHandler {

    private final TelegramMessageSender sender;
    private final UserClient userClient;
    private final RegistrationStateService stateService;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long telegramUserId = update.getMessage().getFrom().getId();

        userClient.getMe(telegramUserId).ifPresentOrElse(
                user -> sender.send(chatId, """
                        Добро пожаловать, %s!
                        Используйте /help, чтобы увидеть доступные команды."""
                        .formatted(user.journalUsername())),
                () -> {
                    stateService.start(telegramUserId);
                    sender.send(chatId, """
                            Вы ещё не зарегистрированы.
                            Для работы бота нужно привязать ваш аккаунт в электронном журнале.
                            
                            Введите логин от журнала:""");
                }
        );
    }
}
