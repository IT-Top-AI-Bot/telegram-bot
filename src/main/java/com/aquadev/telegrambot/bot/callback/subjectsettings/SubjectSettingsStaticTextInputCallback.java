package com.aquadev.telegrambot.bot.callback.subjectsettings;

import com.aquadev.telegrambot.bot.callback.CallbackHandler;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService;
import com.aquadev.telegrambot.client.AutoHomeworkClient;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubjectSettingsStaticTextInputCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final AutoHomeworkClient autoHomeworkClient;
    private final SubjectSettingsStateService stateService;

    @Override
    public boolean supports(String callbackData) {
        return callbackData.startsWith(SubjectSettingsCallbackData.SET_STATIC);
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
        long specId = Long.parseLong(data.substring(SubjectSettingsCallbackData.SET_STATIC.length()));

        List<JournalSpecResponse> specs = autoHomeworkClient.getGroupSpecs(telegramUserId);
        String specName = specs.stream()
                .filter(s -> s.id().equals(specId))
                .findFirst()
                .map(JournalSpecResponse::name)
                .orElse("Предмет #" + specId);

        stateService.startInput(telegramUserId, specId, specName, chatId, messageId,
                SubjectSettingsStateService.InputType.STATIC_TEXT);

        sender.answerCallback(callback.getId());
        sender.editHtml(chatId, messageId,
                SubjectSettingsKeyboardFactory.buildInputStaticText(specName),
                SubjectSettingsKeyboardFactory.buildCancelKeyboard());
    }
}
