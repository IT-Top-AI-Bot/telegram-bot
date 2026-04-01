package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.callback.subjectsettings.SubjectSettingsKeyboardFactory;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService.InputState;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService.InputType;
import com.aquadev.telegrambot.client.ExecutorClient;
import com.aquadev.telegrambot.client.dto.SubjectPromptDto;
import com.aquadev.telegrambot.client.dto.UpsertSubjectPromptRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubjectSettingsTextInputHandler {

    private final TelegramMessageSender sender;
    private final SubjectSettingsStateService stateService;
    private final ExecutorClient executorClient;

    public void handle(Update update) {
        var userMessage = update.getMessage();
        String text = userMessage.getText().trim();
        long userId = userMessage.getFrom().getId();
        long chatId = userMessage.getChatId();
        int userMessageId = userMessage.getMessageId();

        InputState state = stateService.getState(userId);
        stateService.clear(userId);

        sender.deleteMessage(chatId, userMessageId);

        if (state == null) {
            log.warn("No pending state for userId={}", userId);
            return;
        }

        Optional<SubjectPromptDto> current = executorClient.getPromptBySpecId(userId, state.specId());

        UpsertSubjectPromptRequest request;
        if (state.inputType() == InputType.SYSTEM_PROMPT) {
            String currentStatic = current.map(SubjectPromptDto::staticText).orElse(null);
            String currentVision = current.map(SubjectPromptDto::visionPrompt).orElse(null);
            request = new UpsertSubjectPromptRequest(state.specId(), state.specName(), text, currentVision, currentStatic);
        } else {
            String currentPrompt = current.map(SubjectPromptDto::systemPrompt).orElse(null);
            String currentVision = current.map(SubjectPromptDto::visionPrompt).orElse(null);
            request = new UpsertSubjectPromptRequest(state.specId(), state.specName(), currentPrompt, currentVision, text);
        }

        SubjectPromptDto saved = executorClient.upsertPrompt(userId, state.specId(), request);
        log.info("Saved subject settings for userId={}, specId={}, inputType={}", userId, state.specId(), state.inputType());

        sender.editHtml(state.chatId(), state.messageId(),
                SubjectSettingsKeyboardFactory.buildSpecDetailText(state.specName(), saved),
                SubjectSettingsKeyboardFactory.buildSpecDetailKeyboard(state.specId(), saved));
    }
}
