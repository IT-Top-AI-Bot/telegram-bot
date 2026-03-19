package com.aquadev.ittopaitelegrambot.bot.callback.autohomework;

import com.aquadev.ittopaitelegrambot.bot.callback.subjectsettings.SubjectSettingsCallbackData;
import com.aquadev.ittopaitelegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.ittopaitelegrambot.client.dto.JournalSpecResponse;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class AutoHomeworkKeyboardFactory {

    private AutoHomeworkKeyboardFactory() {
    }

    public static String buildSettingsText(AutoHomeworkSettingsResponse settings,
                                           List<JournalSpecResponse> allSpecs) {
        boolean enabled = Boolean.TRUE.equals(settings.enabled());
        String statusLine = enabled ? "✅ Включено" : "❌ Выключено";

        String specsLine;
        if (settings.specIds() == null || settings.specIds().isEmpty()) {
            specsLine = "<i>не выбраны — авто-решение не будет работать</i>";
        } else {
            Map<Long, String> nameById = allSpecs.stream()
                    .collect(Collectors.toMap(s -> s.id(), JournalSpecResponse::name, (existing, replacement) -> existing));
            specsLine = settings.specIds().stream()
                    .sorted()
                    .map(id -> nameById.getOrDefault(id, "ID " + id))
                    .collect(Collectors.joining(", "));
        }

        return ("<b>🤖 Авто-решение домашних заданий</b>\n\n"
                + "Бот отслеживает новые ДЗ в журнале и автоматически отправляет готовый ответ — без вашего участия.\n\n"
                + "Статус: %s\n"
                + "Предметы: %s").formatted(statusLine, specsLine);
    }

    public static InlineKeyboardMarkup buildSettingsKeyboard(AutoHomeworkSettingsResponse settings) {
        boolean enabled = Boolean.TRUE.equals(settings.enabled());
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                        .text(enabled ? "❌ Выключить" : "✅ Включить")
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
