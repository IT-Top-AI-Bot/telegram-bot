package com.aquadev.telegrambot.bot.callback.subjectsettings;

import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
import com.aquadev.telegrambot.client.dto.SubjectPromptDto;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SubjectSettingsKeyboardFactoryTest {

    // ── buildSpecListText ────────────────────────────────────────────────────

    @Test
    void buildSpecListText_containsHeader() {
        String text = SubjectSettingsKeyboardFactory.buildSpecListText(List.of(), List.of());
        assertThat(text).contains("Настройка ответов по предметам");
    }

    // ── buildSpecListKeyboard ────────────────────────────────────────────────

    @Test
    void buildSpecListKeyboard_noSpecs_hasOnlyBackButton() {
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecListKeyboard(List.of(), List.of());
        assertThat(kb.getKeyboard()).hasSize(1);
        assertThat(kb.getKeyboard().get(0).get(0).getText()).contains("Назад");
    }

    @Test
    void buildSpecListKeyboard_withSpec_noPrompt_showsDefaultIndicator() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecListKeyboard(specs, List.of());

        String text = kb.getKeyboard().get(0).get(0).getText();
        assertThat(text).startsWith("⬜").contains("Math");
    }

    @Test
    void buildSpecListKeyboard_withStaticText_showsStaticIndicator() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        var prompts = List.of(new SubjectPromptDto(1L, "Math", null, null, "готовый текст"));
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecListKeyboard(specs, prompts);

        String text = kb.getKeyboard().get(0).get(0).getText();
        assertThat(text).startsWith("📝");
    }

    @Test
    void buildSpecListKeyboard_withSystemPrompt_showsAiIndicator() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        var prompts = List.of(new SubjectPromptDto(1L, "Math", "my prompt", null, null));
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecListKeyboard(specs, prompts);

        String text = kb.getKeyboard().get(0).get(0).getText();
        assertThat(text).startsWith("🤖");
    }

    @Test
    void buildSpecListKeyboard_promptWithBothNullFields_showsDefaultIndicator() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        var prompts = List.of(new SubjectPromptDto(1L, "Math", null, null, null));
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecListKeyboard(specs, prompts);

        String text = kb.getKeyboard().get(0).get(0).getText();
        assertThat(text).startsWith("⬜");
    }

    @Test
    void buildSpecListKeyboard_specCallbackDataContainsSpecId() {
        var specs = List.of(new JournalSpecResponse(42L, "Math", "MTH"));
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecListKeyboard(specs, List.of());

        String data = kb.getKeyboard().get(0).get(0).getCallbackData();
        assertThat(data).isEqualTo(SubjectSettingsCallbackData.SPEC + "42");
    }

    // ── buildSpecDetailText ──────────────────────────────────────────────────

    @Test
    void buildSpecDetailText_nullPrompt_showsNotConfigured() {
        String text = SubjectSettingsKeyboardFactory.buildSpecDetailText("Math", null);
        assertThat(text).contains("не задан");
    }

    @Test
    void buildSpecDetailText_withSystemPrompt_showsSet() {
        var prompt = new SubjectPromptDto(1L, "Math", "my prompt", null, null);
        String text = SubjectSettingsKeyboardFactory.buildSpecDetailText("Math", prompt);
        assertThat(text).contains("задан");
    }

    @Test
    void buildSpecDetailText_withStaticText_showsSet() {
        var prompt = new SubjectPromptDto(1L, "Math", null, null, "static");
        String text = SubjectSettingsKeyboardFactory.buildSpecDetailText("Math", prompt);
        assertThat(text).contains("задан");
    }

    @Test
    void buildSpecDetailText_containsSpecName() {
        String text = SubjectSettingsKeyboardFactory.buildSpecDetailText("Physics", null);
        assertThat(text).contains("Physics");
    }

    // ── buildSpecDetailKeyboard ──────────────────────────────────────────────

    @Test
    void buildSpecDetailKeyboard_noPromptNoStatic_showsSetButtons() {
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecDetailKeyboard(1L, null);

        var firstRow = kb.getKeyboard().get(0);
        assertThat(firstRow).hasSize(1);
        assertThat(firstRow.get(0).getText()).contains("Задать AI промпт");

        var secondRow = kb.getKeyboard().get(1);
        assertThat(secondRow).hasSize(1);
        assertThat(secondRow.get(0).getText()).contains("Задать статический текст");
    }

    @Test
    void buildSpecDetailKeyboard_withPrompt_showsViewEditDeleteButtons() {
        var prompt = new SubjectPromptDto(1L, "Math", "my prompt", null, null);
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecDetailKeyboard(1L, prompt);

        var firstRow = kb.getKeyboard().get(0);
        assertThat(firstRow).hasSize(3);
        assertThat(firstRow.get(0).getText()).contains("Промпт");
        assertThat(firstRow.get(1).getText()).contains("Изменить");
        assertThat(firstRow.get(2).getText()).contains("Удалить");
    }

    @Test
    void buildSpecDetailKeyboard_withStatic_showsViewEditDeleteButtons() {
        var prompt = new SubjectPromptDto(1L, "Math", null, null, "static text");
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecDetailKeyboard(1L, prompt);

        var secondRow = kb.getKeyboard().get(1);
        assertThat(secondRow).hasSize(3);
        assertThat(secondRow.get(0).getText()).contains("Текст");
    }

    @Test
    void buildSpecDetailKeyboard_withBoth_showsAllActionButtons() {
        var prompt = new SubjectPromptDto(1L, "Math", "my prompt", null, "static text");
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecDetailKeyboard(1L, prompt);

        assertThat(kb.getKeyboard().get(0)).hasSize(3);
        assertThat(kb.getKeyboard().get(1)).hasSize(3);
    }

    @Test
    void buildSpecDetailKeyboard_lastRowHasBackButton() {
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecDetailKeyboard(1L, null);
        var lastRow = kb.getKeyboard().get(kb.getKeyboard().size() - 1);
        assertThat(lastRow.get(0).getText()).contains("К списку предметов");
    }

    @Test
    void buildSpecDetailKeyboard_delPromptCallbackContainsSpecId() {
        var prompt = new SubjectPromptDto(1L, "Math", "my prompt", null, null);
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildSpecDetailKeyboard(42L, prompt);

        String delCallback = kb.getKeyboard().get(0).get(2).getCallbackData();
        assertThat(delCallback).isEqualTo(SubjectSettingsCallbackData.DEL_PROMPT + "42");
    }

    // ── buildInputPromptText ─────────────────────────────────────────────────

    @Test
    void buildInputPromptText_containsSpecName() {
        String text = SubjectSettingsKeyboardFactory.buildInputPromptText("Chemistry");
        assertThat(text).contains("Chemistry").contains("AI промпт");
    }

    // ── buildInputStaticText ─────────────────────────────────────────────────

    @Test
    void buildInputStaticText_containsSpecName() {
        String text = SubjectSettingsKeyboardFactory.buildInputStaticText("Biology");
        assertThat(text).contains("Biology").contains("Готовый текст");
    }

    // ── buildCancelKeyboard ──────────────────────────────────────────────────

    @Test
    void buildCancelKeyboard_hasCancelButton() {
        InlineKeyboardMarkup kb = SubjectSettingsKeyboardFactory.buildCancelKeyboard();
        String data = kb.getKeyboard().get(0).get(0).getCallbackData();
        assertThat(data).isEqualTo(SubjectSettingsCallbackData.CANCEL_INPUT);
    }
}
