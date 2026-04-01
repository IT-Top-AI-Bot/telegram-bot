package com.aquadev.telegrambot.client.dto;

public record RegisterRequest(
        String journalUsername,
        String journalPassword
) {
}
