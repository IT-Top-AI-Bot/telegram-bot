package com.aquadev.ittopaitelegrambot.bot.callback.autohomework;

import com.aquadev.ittopaitelegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.ittopaitelegrambot.client.dto.JournalSpecResponse;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AutoHomeworkKeyboardFactoryTest {

    @Test
    void buildSettingsText_enabled_showsEnabledStatus() {
        var settings = new AutoHomeworkSettingsResponse(true, null, Set.of());
        String text = AutoHomeworkKeyboardFactory.buildSettingsText(settings, List.of());

        assertThat(text).contains("✅ Включено");
    }

    @Test
    void buildSettingsText_disabled_showsDisabledStatus() {
        var settings = new AutoHomeworkSettingsResponse(false, null, Set.of());
        String text = AutoHomeworkKeyboardFactory.buildSettingsText(settings, List.of());

        assertThat(text).contains("❌ Выключено");
    }

    @Test
    void buildSettingsText_nullSpecIds_showsNotSelected() {
        var settings = new AutoHomeworkSettingsResponse(true, null, null);
        String text = AutoHomeworkKeyboardFactory.buildSettingsText(settings, List.of());

        assertThat(text).contains("не выбраны");
    }

    @Test
    void buildSettingsText_emptySpecIds_showsNotSelected() {
        var settings = new AutoHomeworkSettingsResponse(true, null, Set.of());
        String text = AutoHomeworkKeyboardFactory.buildSettingsText(settings, List.of());

        assertThat(text).contains("не выбраны");
    }

    @Test
    void buildSettingsText_withSpecs_showsSpecNames() {
        var settings = new AutoHomeworkSettingsResponse(true, null, Set.of(1L, 2L));
        var specs = List.of(
                new JournalSpecResponse(1L, "Math", "MTH"),
                new JournalSpecResponse(2L, "Physics", "PHY")
        );
        String text = AutoHomeworkKeyboardFactory.buildSettingsText(settings, specs);

        assertThat(text).contains("Math").contains("Physics");
    }

    @Test
    void buildSettingsText_unknownSpecId_fallsBackToIdLabel() {
        var settings = new AutoHomeworkSettingsResponse(true, null, Set.of(99L));
        String text = AutoHomeworkKeyboardFactory.buildSettingsText(settings, List.of());

        assertThat(text).contains("ID 99");
    }

    @Test
    void buildSettingsKeyboard_enabled_showsDisableButton() {
        var settings = new AutoHomeworkSettingsResponse(true, null, Set.of());
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSettingsKeyboard(settings);

        String firstButtonText = kb.getKeyboard().get(0).get(0).getText();
        assertThat(firstButtonText).contains("Выключить");
    }

    @Test
    void buildSettingsKeyboard_disabled_showsEnableButton() {
        var settings = new AutoHomeworkSettingsResponse(false, null, Set.of());
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSettingsKeyboard(settings);

        String firstButtonText = kb.getKeyboard().get(0).get(0).getText();
        assertThat(firstButtonText).contains("Включить");
    }

    @Test
    void buildSettingsKeyboard_hasSelectDisciplinesButton() {
        var settings = new AutoHomeworkSettingsResponse(true, null, Set.of());
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSettingsKeyboard(settings);

        String secondButtonText = kb.getKeyboard().get(1).get(0).getText();
        assertThat(secondButtonText).contains("дисциплины");
    }

    @Test
    void buildSpecKeyboard_selectedSpec_hasCheckmark() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSpecKeyboard(specs, Set.of(1L));

        String text = kb.getKeyboard().get(0).get(0).getText();
        assertThat(text).startsWith("✅");
    }

    @Test
    void buildSpecKeyboard_unselectedSpec_hasEmptyBox() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSpecKeyboard(specs, Set.of());

        String text = kb.getKeyboard().get(0).get(0).getText();
        assertThat(text).startsWith("☐");
    }

    @Test
    void buildSpecKeyboard_lastRowHasSaveAndCancelButtons() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSpecKeyboard(specs, Set.of());

        var lastRow = kb.getKeyboard().get(kb.getKeyboard().size() - 1);
        assertThat(lastRow).hasSize(2);
        assertThat(lastRow.get(0).getCallbackData()).isEqualTo(AutoHomeworkCallbackData.SPEC_SAVE);
        assertThat(lastRow.get(1).getCallbackData()).isEqualTo(AutoHomeworkCallbackData.CANCEL);
    }

    @Test
    void buildSpecKeyboard_specCallbackDataContainsSpecId() {
        var specs = List.of(new JournalSpecResponse(42L, "Math", "MTH"));
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSpecKeyboard(specs, Set.of());

        String callbackData = kb.getKeyboard().get(0).get(0).getCallbackData();
        assertThat(callbackData).isEqualTo(AutoHomeworkCallbackData.SPEC_TOGGLE + "42");
    }
}
