package com.aquadev.telegrambot.bot.callback.schedule;

import com.aquadev.telegrambot.client.dto.ScheduleLessonResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class ScheduleMessageFormatter {

    private static final DateTimeFormatter DAY_MONTH = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("ru"));
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private static final Map<DayOfWeek, String> DAY_NAMES = Map.of(
            DayOfWeek.MONDAY, "Понедельник",
            DayOfWeek.TUESDAY, "Вторник",
            DayOfWeek.WEDNESDAY, "Среда",
            DayOfWeek.THURSDAY, "Четверг",
            DayOfWeek.FRIDAY, "Пятница",
            DayOfWeek.SATURDAY, "Суббота",
            DayOfWeek.SUNDAY, "Воскресенье"
    );

    private ScheduleMessageFormatter() {
    }

    public static String format(LocalDate weekStart, List<ScheduleLessonResponse> lessons) {
        Map<LocalDate, List<ScheduleLessonResponse>> byDate = lessons.stream()
                .collect(Collectors.groupingBy(ScheduleLessonResponse::date));

        LocalDate weekEnd = weekStart.plusDays(6);
        StringBuilder sb = new StringBuilder();
        sb.append("📅 <b>").append(weekStart.format(DAY_MONTH))
                .append(" – ").append(weekEnd.format(DAY_MONTH))
                .append(" ").append(weekEnd.getYear()).append("</b>\n");

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            if (day.getDayOfWeek() == DayOfWeek.SUNDAY && !byDate.containsKey(day)) {
                continue;
            }

            sb.append("\n<b>").append(DAY_NAMES.get(day.getDayOfWeek()))
                    .append(", ").append(day.format(DAY_MONTH)).append("</b>\n");

            List<ScheduleLessonResponse> dayLessons = byDate.getOrDefault(day, List.of());
            if (dayLessons.isEmpty()) {
                sb.append("<i>нет занятий</i>\n");
            } else {
                for (ScheduleLessonResponse l : dayLessons) {
                    sb.append(l.lesson()).append(". ")
                            .append(l.startedAt().format(TIME)).append("–").append(l.finishedAt().format(TIME))
                            .append(" · ").append(trimSubject(l.subjectName())).append("\n");
                    sb.append("   👤 ").append(l.teacherName()).append("\n");
                }
            }
        }

        return sb.toString().stripTrailing();
    }

    private static String trimSubject(String name) {
        return name == null ? "" : name.strip().replaceAll("\\s{2,}", " ");
    }
}
