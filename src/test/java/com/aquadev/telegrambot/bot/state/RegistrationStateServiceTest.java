package com.aquadev.telegrambot.bot.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationStateServiceTest {

    private RegistrationStateService service;

    @BeforeEach
    void setUp() {
        service = new RegistrationStateService();
    }

    @Test
    void start_setsAwaitingUsernameStep() {
        service.start(1L);

        assertThat(service.isInProgress(1L)).isTrue();
        assertThat(service.getStep(1L)).isEqualTo(RegistrationStep.AWAITING_USERNAME);
    }

    @Test
    void start_clearsPreviousPendingUsername() {
        service.start(1L);
        service.saveUsernameAndAdvance(1L, "old_user");
        service.start(1L);

        assertThat(service.getPendingUsername(1L)).isNull();
        assertThat(service.getStep(1L)).isEqualTo(RegistrationStep.AWAITING_USERNAME);
    }

    @Test
    void isInProgress_returnsFalse_whenNotStarted() {
        assertThat(service.isInProgress(99L)).isFalse();
    }

    @Test
    void saveUsernameAndAdvance_storesUsernameAndAdvancesToPassword() {
        service.start(1L);
        service.saveUsernameAndAdvance(1L, "johndoe");

        assertThat(service.getStep(1L)).isEqualTo(RegistrationStep.AWAITING_PASSWORD);
        assertThat(service.getPendingUsername(1L)).isEqualTo("johndoe");
    }

    @Test
    void clear_removesAllState() {
        service.start(1L);
        service.saveUsernameAndAdvance(1L, "johndoe");
        service.clear(1L);

        assertThat(service.isInProgress(1L)).isFalse();
        assertThat(service.getStep(1L)).isNull();
        assertThat(service.getPendingUsername(1L)).isNull();
    }

    @Test
    void isolatesDifferentUsers() {
        service.start(1L);
        service.start(2L);
        service.saveUsernameAndAdvance(2L, "user2");

        assertThat(service.getStep(1L)).isEqualTo(RegistrationStep.AWAITING_USERNAME);
        assertThat(service.getStep(2L)).isEqualTo(RegistrationStep.AWAITING_PASSWORD);
        assertThat(service.getPendingUsername(1L)).isNull();
        assertThat(service.getPendingUsername(2L)).isEqualTo("user2");
    }
}

