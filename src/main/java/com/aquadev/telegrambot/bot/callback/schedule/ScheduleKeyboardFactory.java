package com.aquadev.telegrambot.bot.callback.schedule;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class ScheduleKeyboardFactory {

    private ScheduleKeyboardFactory() {
    }

    public static InlineKeyboardMarkup buildNavKeyboard(LocalDate weekStart) {
        LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        boolean isCurrentWeek = weekStart.equals(currentWeekStart);

        var row = new InlineKeyboardRow();
        row.add(InlineKeyboardButton.builder()
                .text("◀️ Пред. неделя")
                .callbackData(ScheduleCallbackData.weekOf(weekStart.minusWeeks(1)))
                .build());
        if (!isCurrentWeek) {
            row.add(InlineKeyboardButton.builder()
                    .text("🗓 Сегодня")
                    .callbackData(ScheduleCallbackData.weekOf(currentWeekStart))
                    .build());
        }
        row.add(InlineKeyboardButton.builder()
                .text("След. неделя ▶️")
                .callbackData(ScheduleCallbackData.weekOf(weekStart.plusWeeks(1)))
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboardRow(row)
                .build();
    }
}
