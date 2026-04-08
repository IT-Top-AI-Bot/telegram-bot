package com.aquadev.telegrambot.client.dto;

public record SubjectPromptDto(
        Long specId,
        String nameSpec,
        String systemPrompt,
        String visionPrompt,
        String staticText
) {
}
