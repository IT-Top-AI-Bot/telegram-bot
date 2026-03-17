package com.aquadev.ittopaitelegrambot.client.dto;

import java.util.Set;

public record UpdateAutoHomeworkSettingsRequest(
        Boolean enabled,
        Set<Long> specIds
) {
}
