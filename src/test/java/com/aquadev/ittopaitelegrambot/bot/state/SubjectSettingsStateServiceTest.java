package com.aquadev.ittopaitelegrambot.bot.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubjectSettingsStateServiceTest {

    SubjectSettingsStateService service;

    @BeforeEach
    void setUp() {
        service = new SubjectSettingsStateService();
    }

    @Test
    void isAwaitingInput_returnsFalse_whenNoState() {
        assertThat(service.isAwaitingInput(99L)).isFalse();
    }

    @Test
    void startInput_setsState_isAwaitingInputReturnsTrue() {
        service.startInput(1L, 10L, "Math", 100L, 5, SubjectSettingsStateService.InputType.SYSTEM_PROMPT);
        assertThat(service.isAwaitingInput(1L)).isTrue();
    }

    @Test
    void getState_returnsCorrectFields() {
        service.startInput(1L, 10L, "Math", 100L, 5, SubjectSettingsStateService.InputType.STATIC_TEXT);

        var state = service.getState(1L);

        assertThat(state.specId()).isEqualTo(10L);
        assertThat(state.specName()).isEqualTo("Math");
        assertThat(state.chatId()).isEqualTo(100L);
        assertThat(state.messageId()).isEqualTo(5);
        assertThat(state.inputType()).isEqualTo(SubjectSettingsStateService.InputType.STATIC_TEXT);
    }

    @Test
    void getState_returnsNull_whenNoState() {
        assertThat(service.getState(99L)).isNull();
    }

    @Test
    void clear_removesState() {
        service.startInput(1L, 10L, "Math", 100L, 5, SubjectSettingsStateService.InputType.SYSTEM_PROMPT);
        service.clear(1L);

        assertThat(service.isAwaitingInput(1L)).isFalse();
        assertThat(service.getState(1L)).isNull();
    }

    @Test
    void startInput_overwritesPreviousState() {
        service.startInput(1L, 10L, "Math", 100L, 5, SubjectSettingsStateService.InputType.SYSTEM_PROMPT);
        service.startInput(1L, 20L, "Physics", 200L, 9, SubjectSettingsStateService.InputType.STATIC_TEXT);

        var state = service.getState(1L);
        assertThat(state.specId()).isEqualTo(20L);
        assertThat(state.inputType()).isEqualTo(SubjectSettingsStateService.InputType.STATIC_TEXT);
    }
}
