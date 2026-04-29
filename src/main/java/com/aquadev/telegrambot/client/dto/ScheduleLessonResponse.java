package com.aquadev.telegrambot.client.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleLessonResponse(
        LocalDate date,
        Integer lesson,
        LocalTime startedAt,
        LocalTime finishedAt,
        String teacherName,
        String subjectName,
        String roomName
) {
}
