package com.aquadev.ittopaitelegrambot.bot.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AutoHomeworkStateServiceTest {

    private AutoHomeworkStateService service;

    @BeforeEach
    void setUp() {
        service = new AutoHomeworkStateService();
    }

    @Test
    void getPendingSpecIds_returnsEmpty_whenNotStarted() {
        assertThat(service.getPendingSpecIds(1L)).isEmpty();
    }

    @Test
    void startSpecSelection_withNullIds_storesEmptySet() {
        service.startSpecSelection(1L, null);
        assertThat(service.getPendingSpecIds(1L)).isEmpty();
    }

    @Test
    void startSpecSelection_withIds_storesCopy() {
        Set<Long> initial = Set.of(10L, 20L);
        service.startSpecSelection(1L, initial);

        assertThat(service.getPendingSpecIds(1L)).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void startSpecSelection_doesNotMutateOriginal() {
        Set<Long> external = new java.util.HashSet<>(Set.of(10L));
        service.startSpecSelection(1L, external);
        service.toggleSpec(1L, 20L);

        assertThat(external).containsExactly(10L);
    }

    @Test
    void toggleSpec_addsSpec_whenNotPresent() {
        service.startSpecSelection(1L, Set.of());
        service.toggleSpec(1L, 5L);

        assertThat(service.getPendingSpecIds(1L)).containsExactly(5L);
    }

    @Test
    void toggleSpec_removesSpec_whenAlreadyPresent() {
        service.startSpecSelection(1L, Set.of(5L));
        service.toggleSpec(1L, 5L);

        assertThat(service.getPendingSpecIds(1L)).isEmpty();
    }

    @Test
    void toggleSpec_isNoOp_whenUserHasNoState() {
        service.toggleSpec(99L, 5L);
        assertThat(service.getPendingSpecIds(99L)).isEmpty();
    }

    @Test
    void getPendingSpecIds_returnsImmutableCopy() {
        service.startSpecSelection(1L, Set.of(1L));
        Set<Long> result = service.getPendingSpecIds(1L);

        assertThat(result).containsExactly(1L);
        // returned copy doesn't reflect subsequent changes
        service.toggleSpec(1L, 2L);
        assertThat(result).containsExactly(1L);
    }

    @Test
    void clear_removesState() {
        service.startSpecSelection(1L, Set.of(10L));
        service.clear(1L);

        assertThat(service.getPendingSpecIds(1L)).isEmpty();
    }

    @Test
    void isolatesDifferentUsers() {
        service.startSpecSelection(1L, Set.of(1L));
        service.startSpecSelection(2L, Set.of(2L));

        assertThat(service.getPendingSpecIds(1L)).containsExactly(1L);
        assertThat(service.getPendingSpecIds(2L)).containsExactly(2L);
    }
}
