package com.aquadev.telegrambot.client.dto;

public record JournalGroupInfoResponse(
        Integer groupStatus,
        Boolean isPrimary,
        Long id,
        String name
) {
}
