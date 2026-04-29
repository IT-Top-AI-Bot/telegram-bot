package com.aquadev.telegrambot.bot.callback.schedule;

import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.ScheduleClient;
import com.aquadev.telegrambot.client.dto.ScheduleLessonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleClient scheduleClient;
    private final TelegramMessageSender sender;

    public void sendScheduleMessage(long chatId, long telegramUserId, LocalDate weekStart) {
        List<ScheduleLessonResponse> lessons = scheduleClient.getWeekSchedule(telegramUserId, weekStart);
        String text = ScheduleMessageFormatter.format(weekStart, lessons);
        sender.sendHtml(chatId, text, ScheduleKeyboardFactory.buildNavKeyboard(weekStart));
    }

    public void editScheduleMessage(long chatId, int messageId, long telegramUserId, LocalDate weekStart) {
        List<ScheduleLessonResponse> lessons = scheduleClient.getWeekSchedule(telegramUserId, weekStart);
        String text = ScheduleMessageFormatter.format(weekStart, lessons);
        sender.editHtml(chatId, messageId, text, ScheduleKeyboardFactory.buildNavKeyboard(weekStart));
    }
}
