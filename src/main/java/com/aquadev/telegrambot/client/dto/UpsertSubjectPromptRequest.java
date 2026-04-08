package com.aquadev.telegrambot.client.dto;

public record UpsertSubjectPromptRequest(
        Long specId,
        String nameSpec,
        String systemPrompt,
        String visionPrompt,
        String staticText
) {
}
