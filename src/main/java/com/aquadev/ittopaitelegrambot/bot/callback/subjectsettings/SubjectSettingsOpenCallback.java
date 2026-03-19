package com.aquadev.ittopaitelegrambot.bot.callback.subjectsettings;

import com.aquadev.ittopaitelegrambot.bot.callback.CallbackHandler;
import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import com.aquadev.ittopaitelegrambot.client.AutoHomeworkClient;
import com.aquadev.ittopaitelegrambot.client.ExecutorClient;
import com.aquadev.ittopaitelegrambot.client.dto.JournalSpecResponse;
import com.aquadev.ittopaitelegrambot.client.dto.SubjectPromptDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubjectSettingsOpenCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final AutoHomeworkClient autoHomeworkClient;
    private final ExecutorClient executorClient;

    @Override
    public boolean supports(String callbackData) {
        return SubjectSettingsCallbackData.OPEN.equals(callbackData);
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

        List<JournalSpecResponse> specs = autoHomeworkClient.getGroupSpecs(telegramUserId);
        List<SubjectPromptDto> prompts = executorClient.getAllPrompts(telegramUserId);

        sender.answerCallback(callback.getId());
        sender.editHtml(chatId, messageId,
                SubjectSettingsKeyboardFactory.buildSpecListText(specs, prompts),
                SubjectSettingsKeyboardFactory.buildSpecListKeyboard(specs, prompts));
    }
}
