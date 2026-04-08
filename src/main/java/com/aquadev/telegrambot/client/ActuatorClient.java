package com.aquadev.telegrambot.client;

import com.aquadev.telegrambot.config.properties.AdminProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ActuatorClient {

    private static final String REFRESH_PATH = "/actuator/refresh";
    private static final ParameterizedTypeReference<List<String>> STRING_LIST = new ParameterizedTypeReference<>() {
    };

    private final RestClient actuatorRestClient;
    private final AdminProperties adminProperties;

    public ActuatorClient(@Qualifier("actuatorRestClient") RestClient actuatorRestClient,
                          AdminProperties adminProperties) {
        this.actuatorRestClient = actuatorRestClient;
        this.adminProperties = adminProperties;
    }

    /**
     * Вызывает POST /actuator/refresh на каждом сервисе.
     *
     * @return map: название сервиса → список обновлённых ключей или сообщение об ошибке
     */
    public Map<String, String> refreshAll() {
        Map<String, String> results = new LinkedHashMap<>();
        List<AdminProperties.ServiceConfig> services = adminProperties.services();
        if (services == null || services.isEmpty()) {
            return results;
        }
        for (AdminProperties.ServiceConfig service : services) {
            results.put(service.name(), refreshService(service));
        }
        return results;
    }

    private String refreshService(AdminProperties.ServiceConfig service) {
        try {
            List<String> changedKeys = actuatorRestClient.post()
                    .uri(service.actuatorUrl() + REFRESH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(STRING_LIST);

            if (changedKeys == null || changedKeys.isEmpty()) {
                return "✅ Нет изменений";
            }
            return "✅ Обновлено: " + String.join(", ", changedKeys);
        } catch (RestClientException e) {
            log.error("Actuator refresh failed for service '{}' at {}: {}", service.name(), service.actuatorUrl(), e.getMessage());
            return "❌ Ошибка: " + e.getMessage();
        }
    }
}
