package com.aquadev.ittopaitelegrambot.bot.callback.subjectsettings;

import com.aquadev.ittopaitelegrambot.bot.callback.CallbackHandler;
import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import com.aquadev.ittopaitelegrambot.client.ExecutorClient;
import com.aquadev.ittopaitelegrambot.client.dto.SubjectPromptDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubjectSettingsViewTextCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final ExecutorClient executorClient;

    @Override
    public boolean supports(String callbackData) {
        return callbackData.startsWith(SubjectSettingsCallbackData.VIEW_PROMPT)
                || callbackData.startsWith(SubjectSettingsCallbackData.VIEW_STATIC);
    }

    @Override
    public void handle(Update update) {
        var callback = update.getCallbackQuery();
        long telegramUserId = callback.getFrom().getId();
        String data = callback.getData();

        boolean isPrompt = data.startsWith(SubjectSettingsCallbackData.VIEW_PROMPT);
        String prefix = isPrompt ? SubjectSettingsCallbackData.VIEW_PROMPT : SubjectSettingsCallbackData.VIEW_STATIC;
        long specId = Long.parseLong(data.substring(prefix.length()));

        Optional<SubjectPromptDto> prompt = executorClient.getPromptBySpecId(telegramUserId, specId);
        String text = prompt.map(p -> isPrompt ? p.systemPrompt() : p.staticText()).orElse(null);

        if (text == null) {
            sender.answerCallback(callback.getId(), "Текст не задан");
            return;
        }

        sender.answerCallbackAlert(callback.getId(), text);
    }
}
