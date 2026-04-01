package com.aquadev.telegrambot.client.dto;

import java.util.Set;

public record UpdateAutoHomeworkSettingsRequest(
        Boolean enabled,
        Set<Long> specIds
) {
}
