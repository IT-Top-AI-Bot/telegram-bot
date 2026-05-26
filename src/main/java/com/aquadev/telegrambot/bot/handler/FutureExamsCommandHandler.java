package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.JournalClient;
import com.aquadev.telegrambot.client.dto.JournalFutureExamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@TelegramBotCommand(value = "/future_exams", description = "Список предстоящих экзаменов")
public class FutureExamsCommandHandler implements CommandHandler {

    private final JournalClient journalClient;
    private final TelegramMessageSender sender;

    @Override
    public void handle(Update update) {
        long telegramId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();

        List<JournalFutureExamResponse> futureExams = journalClient.getFutureExams(telegramId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM (EEEE)", Locale.of("ru"));

        String examsList = futureExams.stream()
                .map(exam -> {
                    String formattedDate = exam.date().format(formatter);

                    return String.format(
                            "📖 <b>%s</b>\n└ %s",
                            exam.spec(),
                            formattedDate
                    );
                })
                .collect(Collectors.joining("\n\n"));

        String messageText = String.format("""
                <b>🗓 Назначенные экзамены:</b>
                
                %s
                """, examsList);

        sender.sendHtml(chatId, messageText);
    }

}
