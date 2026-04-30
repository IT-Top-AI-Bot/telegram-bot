package com.aquadev.telegrambot.client.dto;

import java.time.LocalDate;
import java.util.List;

public record JournalProfileResponse(
        List<JournalGroupInfoResponse> groups,
        String fullName,
        Integer achievesCount,
        String streamName,
        Integer level,
        String photo,
        List<JournalGamePointResponse> gamingPoints,
        LocalDate birthday,
        Short age
) {
}
