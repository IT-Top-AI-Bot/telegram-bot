package com.aquadev.ittopaitelegrambot.bot.handler;

import com.aquadev.ittopaitelegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.ittopaitelegrambot.bot.state.RegistrationStateService;
import com.aquadev.ittopaitelegrambot.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
@TelegramBotCommand(value = "/start", description = "Запустить бота")
public class StartCommandHandler implements CommandHandler {

    private final TelegramClient telegramClient;
    private final UserClient userClient;
    private final RegistrationStateService stateService;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long telegramUserId = update.getMessage().getFrom().getId();

        userClient.getMe(telegramUserId).ifPresentOrElse(
                user -> send(chatId, """
                        Добро пожаловать, %s!
                        Используйте /help, чтобы увидеть доступные команды."""
                        .formatted(user.journalUsername())),
                () -> {
                    stateService.start(telegramUserId);
                    send(chatId, """
                            Вы ещё не зарегистрированы.
                            Давайте создадим аккаунт — введите желаемый username:""");
                }
        );
    }

    private void send(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Не удалось отправить ответ на /start", e);
        }
    }
}
