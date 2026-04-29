package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.telegrambot.bot.callback.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@TelegramBotCommand(value = "/schedule", description = "Расписание занятий на неделю")
public class ScheduleCommandHandler implements CommandHandler {

    private final ScheduleService scheduleService;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long telegramUserId = update.getMessage().getFrom().getId();
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        scheduleService.sendScheduleMessage(chatId, telegramUserId, weekStart);
    }
}
