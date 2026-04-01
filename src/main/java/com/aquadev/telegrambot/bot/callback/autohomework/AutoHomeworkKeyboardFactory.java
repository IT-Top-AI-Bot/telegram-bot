package com.aquadev.telegrambot.bot.callback.autohomework;

import com.aquadev.telegrambot.bot.callback.subjectsettings.SubjectSettingsCallbackData;
import com.aquadev.telegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.Set;


public final class AutoHomeworkKeyboardFactory {

    private AutoHomeworkKeyboardFactory() {
    }

    public static String buildSettingsText(AutoHomeworkSettingsResponse settings) {
        boolean enabled = Boolean.TRUE.equals(settings.enabled());
        String statusLine = enabled ? "✅ Включено" : "❌ Выключено";

        return ("<b>🤖 Авто-решение домашних заданий</b>\n\n"
                + "Бот отслеживает новые ДЗ в журнале и автоматически отправляет готовый ответ — без вашего участия.\n\n"
                + "Статус: %s").formatted(statusLine);
    }

    public static InlineKeyboardMarkup buildSettingsKeyboard(AutoHomeworkSettingsResponse settings) {
        boolean enabled = Boolean.TRUE.equals(settings.enabled());
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                        .text(enabled ? "❌ Выключить" : "✅ Включить")
                        .style(enabled ? "danger" : "success")
                        .callbackData(enabled ? AutoHomeworkCallbackData.SET_ENABLED + "false"
                                : AutoHomeworkCallbackData.SET_ENABLED + "true")
                        .build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                        .text("📚 Выбрать предметы")
                        .callbackData(AutoHomeworkCallbackData.OPEN_SPECS)
                        .build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                        .text("✏️ Настройка ответов по предметам")
                        .callbackData(SubjectSettingsCallbackData.OPEN)
                        .build()))
                .build();
    }

    public static InlineKeyboardMarkup buildSpecKeyboard(List<JournalSpecResponse> specs,
                                                         Set<Long> selectedIds) {
        var builder = InlineKeyboardMarkup.builder();
        boolean allSelected = !specs.isEmpty() && specs.stream().map(JournalSpecResponse::id).allMatch(selectedIds::contains);
        builder.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text(allSelected ? "Выключить все предметы" : "Включить все предметы")
                .style(allSelected ? "danger" : "success")
                .callbackData(AutoHomeworkCallbackData.SPEC_TOGGLE_ALL)
                .build()));
        for (JournalSpecResponse spec : specs) {
            boolean selected = selectedIds.contains(spec.id());
            builder.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text((selected ? "✅ " : "☐ ") + spec.name())
                    .callbackData(AutoHomeworkCallbackData.SPEC_TOGGLE + spec.id())
                    .build()));
        }
        builder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("💾 Сохранить")
                        .style("primary")
                        .callbackData(AutoHomeworkCallbackData.SPEC_SAVE)
                        .build(),
                InlineKeyboardButton.builder()
                        .text("↩ Назад")
                        .callbackData(AutoHomeworkCallbackData.CANCEL)
                        .build()
        ));
        return builder.build();
    }
}
