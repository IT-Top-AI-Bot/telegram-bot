package com.aquadev.ittopaitelegrambot.client.dto;

public record RegisterRequest(
        String journalUsername,
        String journalPassword
) {
}
