package com.aquadev.ittopaitelegrambot.client.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        Long telegramId,
        String journalUsername,
        Instant createdAt,
        Instant updatedAt
) {
}
