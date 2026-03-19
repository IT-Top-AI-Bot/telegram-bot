package com.aquadev.ittopaitelegrambot.bot.state;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SubjectSettingsStateService {

    public enum InputType {
        SYSTEM_PROMPT,
        STATIC_TEXT
    }

    public record InputState(long specId, String specName, long chatId, int messageId, InputType inputType) {
    }

    private final Map<Long, InputState> states = new ConcurrentHashMap<>();

    public void startInput(long userId, long specId, String specName, long chatId, int messageId, InputType inputType) {
        states.put(userId, new InputState(specId, specName, chatId, messageId, inputType));
    }

    public boolean isAwaitingInput(long userId) {
        return states.containsKey(userId);
    }

    public InputState getState(long userId) {
        return states.get(userId);
    }

    public void clear(long userId) {
        states.remove(userId);
    }
}
