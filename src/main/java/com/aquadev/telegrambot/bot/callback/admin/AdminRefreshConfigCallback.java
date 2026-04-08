package com.aquadev.telegrambot.bot.callback.admin;

import com.aquadev.telegrambot.bot.callback.CallbackHandler;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.ActuatorClient;
import com.aquadev.telegrambot.config.properties.AdminProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.StringJoiner;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminRefreshConfigCallback implements CallbackHandler {

    private final TelegramMessageSender sender;
    private final ActuatorClient actuatorClient;
    private final AdminProperties adminProperties;

    @Override
    public boolean supports(String callbackData) {
        return AdminCallbackData.REFRESH_CONFIG.equals(callbackData);
    }

    @Override
    public void handle(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long telegramUserId = update.getCallbackQuery().getFrom().getId();
        String callbackId = update.getCallbackQuery().getId();

        if (!adminProperties.isAdmin(telegramUserId)) {
            sender.answerCallbackAlert(callbackId, "⛔ Доступ запрещён");
            return;
        }

        sender.answerCallback(callbackId, "⏳ Обновляю конфиг...");

        if (adminProperties.services() == null || adminProperties.services().isEmpty()) {
            sender.send(chatId, "⚠️ Сервисы для обновления не настроены.\nДобавьте <code>admin.services</code> в конфигурацию.");
            return;
        }

        log.info("Admin {} triggered config refresh for {} service(s)", telegramUserId, adminProperties.services().size());

        Map<String, String> results = actuatorClient.refreshAll();

        StringJoiner report = new StringJoiner("\n");
        report.add("<b>🔄 Результат обновления конфига:</b>\n");
        results.forEach((service, result) ->
                report.add("<b>" + service + "</b>: " + result));

        sender.sendHtml(chatId, report.toString(), AdminKeyboardFactory.buildPanelKeyboard());
    }
}
