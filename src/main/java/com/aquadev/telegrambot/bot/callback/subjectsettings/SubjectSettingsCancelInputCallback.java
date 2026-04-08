package com.aquadev.telegrambot.bot.callback.subjectsettings;

import com.aquadev.telegrambot.bot.callback.CallbackHandler;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService;
import com.aquadev.telegrambot.bot.state.SubjectSettingsStateService.InputState;
import com.aquadev.telegrambot.client.ExecutorClient;
import com.aquadev.telegrambot.client.dto.SubjectPromptDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubjectSettingsCancelInputCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final SubjectSettingsStateService stateService;
    private final ExecutorClient executorClient;

    @Override
    public boolean supports(String callbackData) {
        return SubjectSettingsCallbackData.CANCEL_INPUT.equals(callbackData);
    }

    @Override
    public void handle(Update update) {
        var callback = update.getCallbackQuery();
        if (!(callback.getMessage() instanceof Message)) {
            sender.answerCallback(callback.getId(), "Сообщение устарело");
            return;
        }
        long telegramUserId = callback.getFrom().getId();

        InputState state = stateService.getState(telegramUserId);
        stateService.clear(telegramUserId);

        if (state == null) {
            sender.answerCallback(callback.getId());
            return;
        }

        Optional<SubjectPromptDto> prompt = executorClient.getPromptBySpecId(telegramUserId, state.specId());

        sender.answerCallback(callback.getId());
        sender.editHtml(state.chatId(), state.messageId(),
                SubjectSettingsKeyboardFactory.buildSpecDetailText(state.specName(), prompt.orElse(null)),
                SubjectSettingsKeyboardFactory.buildSpecDetailKeyboard(state.specId(), prompt.orElse(null)));
    }
}
