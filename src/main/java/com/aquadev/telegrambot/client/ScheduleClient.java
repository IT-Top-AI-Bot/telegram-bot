package com.aquadev.telegrambot.client;

import com.aquadev.telegrambot.client.dto.ScheduleLessonResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Component
public class ScheduleClient extends BackendClient {

    public ScheduleClient(RestClient restClient) {
        super(restClient);
    }

    public List<ScheduleLessonResponse> getWeekSchedule(long telegramUserId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        String uri = "/api/v1/telegram/journal/schedule/range?dateStart=" + weekStart + "&dateEnd=" + weekEnd;
        return get(telegramUserId, uri, new ParameterizedTypeReference<>() {
        });
    }
}
