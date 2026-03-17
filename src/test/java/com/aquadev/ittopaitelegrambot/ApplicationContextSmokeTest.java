package com.aquadev.ittopaitelegrambot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Smoke test: verifies the Spring application context loads without errors.
 * Uses application-test.yaml for Kubernetes/Telegram stubs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ApplicationContextSmokeTest {

    @MockitoBean
    TelegramClient telegramClient;

    @Test
    void contextLoads() {
        // If Spring context loads without exception, this test passes.
    }
}
