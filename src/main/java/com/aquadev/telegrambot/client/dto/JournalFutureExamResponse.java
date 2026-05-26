package com.aquadev.telegrambot.client.dto;

import java.time.LocalDate;

public record JournalFutureExamResponse(
        String spec,
        LocalDate date
) {
}
