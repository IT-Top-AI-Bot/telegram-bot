package com.aquadev.telegrambot.bot.state;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AutoHomeworkStateService {

    private final Map<Long, Set<Long>> pendingSpecIds = new ConcurrentHashMap<>();

    public void startSpecSelection(long userId, Set<Long> currentSpecIds) {
        pendingSpecIds.put(userId, currentSpecIds != null ? new HashSet<>(currentSpecIds) : new HashSet<>());
    }

    public void toggleSpec(long userId, long specId) {
        Set<Long> specs = pendingSpecIds.get(userId);
        if (specs == null) return;
        if (!specs.remove(specId)) specs.add(specId);
    }

    public void setSpecs(long userId, Set<Long> specIds) {
        Set<Long> specs = pendingSpecIds.get(userId);
        if (specs == null) return;
        specs.clear();
        specs.addAll(specIds);
    }

    public Set<Long> getPendingSpecIds(long userId) {
        Set<Long> specs = pendingSpecIds.get(userId);
        return specs != null ? Set.copyOf(specs) : Set.of();
    }

    public void clear(long userId) {
        pendingSpecIds.remove(userId);
    }
}
