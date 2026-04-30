package com.aquadev.telegrambot.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JournalGamePointResponse(
        @JsonProperty("new_gaming_point_types__id")
        Integer typeId,
        Integer points
) {
}
