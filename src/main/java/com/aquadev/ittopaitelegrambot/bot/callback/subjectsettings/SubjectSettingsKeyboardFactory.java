package com.aquadev.ittopaitelegrambot.bot.callback.subjectsettings;

import com.aquadev.ittopaitelegrambot.client.dto.JournalSpecResponse;
import com.aquadev.ittopaitelegrambot.client.dto.SubjectPromptDto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SubjectSettingsKeyboardFactory {

    private SubjectSettingsKeyboardFactory() {
    }

    public static String buildSpecListText(List<JournalSpecResponse> specs,
                                           List<SubjectPromptDto> prompts) {
        return "✏️ <b>Настройка ответов по предметам</b>\n\n"
                + "Выберите предмет, чтобы задать тип ответа:\n\n"
                + "⬜ — стандартный AI\n"
                + "🤖 — кастомный AI промпт\n"
                + "📝 — готовый текст (без AI)";
    }

    public static InlineKeyboardMarkup buildSpecListKeyboard(List<JournalSpecResponse> specs,
                                                             List<SubjectPromptDto> prompts) {
        Map<Long, SubjectPromptDto> promptBySpecId = prompts.stream()
                .collect(Collectors.toMap(SubjectPromptDto::specId, p -> p));

        var builder = InlineKeyboardMarkup.builder();
        for (JournalSpecResponse spec : specs) {
            SubjectPromptDto prompt = promptBySpecId.get(spec.id());
            String indicator = buildIndicator(prompt);
            builder.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text(indicator + " " + spec.name())
                    .callbackData(SubjectSettingsCallbackData.SPEC + spec.id())
                    .build()));
        }
        builder.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text("↩ Назад")
                .callbackData(com.aquadev.ittopaitelegrambot.bot.callback.autohomework.AutoHomeworkCallbackData.CANCEL)
                .build()));
        return builder.build();
    }

    public static String buildSpecDetailText(String specName, SubjectPromptDto prompt) {
        String promptStatus = prompt != null && prompt.systemPrompt() != null ? "задан" : "<i>не задан</i>";
        String staticStatus = prompt != null && prompt.staticText() != null ? "задан" : "<i>не задан</i>";

        return """
                ✏️ <b>%s</b>
                
                🤖 <b>AI промпт:</b> %s
                📝 <b>Готовый текст:</b> %s
                
                <i>Если задан готовый текст — он отправляется без участия AI. Если только AI промпт — используется он. Если ничего не задано — стандартный AI.</i>""".formatted(specName, promptStatus, staticStatus);
    }

    public static InlineKeyboardMarkup buildSpecDetailKeyboard(Long specId, SubjectPromptDto prompt) {
        var builder = InlineKeyboardMarkup.builder();

        boolean hasPrompt = prompt != null && prompt.systemPrompt() != null;
        boolean hasStatic = prompt != null && prompt.staticText() != null;

        if (hasPrompt) {
            builder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text("👁 Промпт")
                            .callbackData(SubjectSettingsCallbackData.VIEW_PROMPT + specId)
                            .build(),
                    InlineKeyboardButton.builder()
                            .text("✏️ Изменить")
                            .callbackData(SubjectSettingsCallbackData.SET_PROMPT + specId)
                            .build(),
                    InlineKeyboardButton.builder()
                            .text("🗑 Удалить")
                            .callbackData(SubjectSettingsCallbackData.DEL_PROMPT + specId)
                            .build()
            ));
        } else {
            builder.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text("🤖 Задать AI промпт")
                    .callbackData(SubjectSettingsCallbackData.SET_PROMPT + specId)
                    .build()));
        }

        if (hasStatic) {
            builder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text("👁 Текст")
                            .callbackData(SubjectSettingsCallbackData.VIEW_STATIC + specId)
                            .build(),
                    InlineKeyboardButton.builder()
                            .text("✏️ Изменить")
                            .callbackData(SubjectSettingsCallbackData.SET_STATIC + specId)
                            .build(),
                    InlineKeyboardButton.builder()
                            .text("🗑 Удалить")
                            .callbackData(SubjectSettingsCallbackData.DEL_STATIC + specId)
                            .build()
            ));
        } else {
            builder.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text("📝 Задать статический текст")
                    .callbackData(SubjectSettingsCallbackData.SET_STATIC + specId)
                    .build()));
        }

        builder.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text("↩ К списку предметов")
                .callbackData(SubjectSettingsCallbackData.OPEN)
                .build()));
        return builder.build();
    }

    public static String buildInputPromptText(String specName) {
        return ("🤖 <b>AI промпт — %s</b>\n\n"
                + "Введите дополнительную инструкцию для AI. Она будет добавлена к базовому промпту при решении ДЗ по этому предмету.\n\n"
                + "<i>Например: «Отвечай максимально кратко» или «Добавь побольше своих рассуждений»</i>").formatted(specName);
    }

    public static String buildInputStaticText(String specName) {
        return ("📝 <b>Готовый текст — %s</b>\n\n"
                + "Введите текст, который бот будет автоматически отправлять как ответ на ДЗ по этому предмету. AI участвовать не будет.\n\n"
                + "<i>Подходит для предметов, где ответ всегда одинаковый или уже заготовлен.</i>").formatted(specName);
    }

    public static InlineKeyboardMarkup buildCancelKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                        .text("❌ Отмена")
                        .callbackData(SubjectSettingsCallbackData.CANCEL_INPUT)
                        .build()))
                .build();
    }

    private static String buildIndicator(SubjectPromptDto prompt) {
        if (prompt == null) return "⬜";
        if (prompt.staticText() != null) return "📝";
        if (prompt.systemPrompt() != null) return "🤖";
        return "⬜";
    }
}
