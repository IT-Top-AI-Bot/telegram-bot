package com.aquadev.ittopaitelegrambot.client.dto;

import java.time.Instant;
import java.util.Set;

public record AutoHomeworkSettingsResponse(
        Boolean enabled,
        Instant lastCheckedAt,
        Set<Long> specIds
) {
}
