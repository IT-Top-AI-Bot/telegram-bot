package com.aquadev.telegrambot.bot.callback.schedule;

import com.aquadev.telegrambot.bot.callback.CallbackHandler;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ScheduleWeekCallback implements CallbackHandler {

    private final ScheduleService scheduleService;
    private final TelegramMessageSender sender;

    @Override
    public boolean supports(String callbackData) {
        return callbackData.startsWith(ScheduleCallbackData.WEEK);
    }

    @Override
    public void handle(Update update) {
        var query = update.getCallbackQuery();
        long chatId = query.getMessage().getChatId();
        int messageId = query.getMessage().getMessageId();
        long telegramUserId = query.getFrom().getId();

        String dateStr = query.getData().substring(ScheduleCallbackData.WEEK.length());
        LocalDate weekStart = LocalDate.parse(dateStr);

        sender.answerCallback(query.getId());
        scheduleService.editScheduleMessage(chatId, messageId, telegramUserId, weekStart);
    }
}
