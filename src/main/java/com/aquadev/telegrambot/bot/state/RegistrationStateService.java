package com.aquadev.telegrambot.bot.state;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistrationStateService {

    private final Map<Long, RegistrationStep> steps = new ConcurrentHashMap<>();
    private final Map<Long, String> pendingUsernames = new ConcurrentHashMap<>();
    private final Set<Long> updateMode = ConcurrentHashMap.newKeySet();

    public void start(long telegramUserId) {
        pendingUsernames.remove(telegramUserId);
        updateMode.remove(telegramUserId);
        steps.put(telegramUserId, RegistrationStep.AWAITING_USERNAME);
    }

    public void startUpdate(long telegramUserId) {
        pendingUsernames.remove(telegramUserId);
        updateMode.add(telegramUserId);
        steps.put(telegramUserId, RegistrationStep.AWAITING_USERNAME);
    }

    public boolean isUpdate(long telegramUserId) {
        return updateMode.contains(telegramUserId);
    }

    public boolean isInProgress(long telegramUserId) {
        return steps.containsKey(telegramUserId);
    }

    public RegistrationStep getStep(long telegramUserId) {
        return steps.get(telegramUserId);
    }

    public void saveUsernameAndAdvance(long telegramUserId, String username) {
        pendingUsernames.put(telegramUserId, username);
        steps.put(telegramUserId, RegistrationStep.AWAITING_PASSWORD);
    }

    public String getPendingUsername(long telegramUserId) {
        return pendingUsernames.get(telegramUserId);
    }

    public void clear(long telegramUserId) {
        steps.remove(telegramUserId);
        pendingUsernames.remove(telegramUserId);
        updateMode.remove(telegramUserId);
    }
}
