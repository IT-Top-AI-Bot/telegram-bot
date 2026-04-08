package com.aquadev.telegrambot.bot.callback.subjectsettings;

import com.aquadev.telegrambot.bot.callback.CallbackHandler;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.AutoHomeworkClient;
import com.aquadev.telegrambot.client.ExecutorClient;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
import com.aquadev.telegrambot.client.dto.SubjectPromptDto;
import com.aquadev.telegrambot.client.dto.UpsertSubjectPromptRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubjectSettingsDeletePromptCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final AutoHomeworkClient autoHomeworkClient;
    private final ExecutorClient executorClient;

    @Override
    public boolean supports(String callbackData) {
        return callbackData.startsWith(SubjectSettingsCallbackData.DEL_PROMPT);
    }

    @Override
    public void handle(Update update) {
        var callback = update.getCallbackQuery();
        if (!(callback.getMessage() instanceof Message message)) {
            sender.answerCallback(callback.getId(), "Сообщение устарело");
            return;
        }
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        long telegramUserId = callback.getFrom().getId();

        String data = callback.getData();
        long specId = Long.parseLong(data.substring(SubjectSettingsCallbackData.DEL_PROMPT.length()));

        List<JournalSpecResponse> specs = autoHomeworkClient.getGroupSpecs(telegramUserId);
        String specName = specs.stream()
                .filter(s -> s.id().equals(specId))
                .findFirst()
                .map(JournalSpecResponse::name)
                .orElse("Предмет #" + specId);

        Optional<SubjectPromptDto> current = executorClient.getPromptBySpecId(telegramUserId, specId);
        SubjectPromptDto result = null;

        if (current.isPresent()) {
            String staticText = current.get().staticText();
            if (staticText != null) {
                // Keep static text, clear only AI prompt
                result = executorClient.upsertPrompt(telegramUserId, specId,
                        new UpsertSubjectPromptRequest(specId, specName, null, null, staticText));
            } else {
                // Both fields would be null — delete the record entirely
                executorClient.deletePrompt(telegramUserId, specId);
            }
        }

        sender.answerCallback(callback.getId(), "🗑 AI промпт удалён");
        sender.editHtml(chatId, messageId,
                SubjectSettingsKeyboardFactory.buildSpecDetailText(specName, result),
                SubjectSettingsKeyboardFactory.buildSpecDetailKeyboard(specId, result));
    }
}
