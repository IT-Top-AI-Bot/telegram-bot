package com.aquadev.telegrambot.bot.callback.autohomework;

import com.aquadev.telegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.telegrambot.client.dto.JournalSpecResponse;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AutoHomeworkKeyboardFactoryTest {

    @Test
    void buildSettingsText_enabled_showsEnabledStatus() {
        var settings = new AutoHomeworkSettingsResponse(true, null, Set.of());
        String text = AutoHomeworkKeyboardFactory.buildSettingsText(settings);

        assertThat(text).contains("✅ Включено");
    }

    @Test
    void buildSettingsText_disabled_showsDisabledStatus() {
        var settings = new AutoHomeworkSettingsResponse(false, null, Set.of());
        String text = AutoHomeworkKeyboardFactory.buildSettingsText(settings);

        assertThat(text).contains("❌ Выключено");
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
        assertThat(secondButtonText).contains("предметы");
    }

    @Test
    void buildSpecKeyboard_selectedSpec_hasCheckmark() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSpecKeyboard(specs, Set.of(1L));

        // row 0 is toggle-all, spec buttons start at row 1
        String text = kb.getKeyboard().get(1).get(0).getText();
        assertThat(text).startsWith("✅");
    }

    @Test
    void buildSpecKeyboard_unselectedSpec_hasEmptyBox() {
        var specs = List.of(new JournalSpecResponse(1L, "Math", "MTH"));
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSpecKeyboard(specs, Set.of());

        // row 0 is toggle-all, spec buttons start at row 1
        String text = kb.getKeyboard().get(1).get(0).getText();
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

        // row 0 is toggle-all, row 1 is the spec button
        String callbackData = kb.getKeyboard().get(1).get(0).getCallbackData();
        assertThat(callbackData).isEqualTo(AutoHomeworkCallbackData.SPEC_TOGGLE + "42");
    }

    @Test
    void buildSpecKeyboard_notAllSelected_showsIncludeAllButton() {
        var specs = List.of(
                new JournalSpecResponse(1L, "Math", "MTH"),
                new JournalSpecResponse(2L, "Physics", "PHY")
        );
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSpecKeyboard(specs, Set.of(1L));

        var toggleAllRow = kb.getKeyboard().get(0);
        assertThat(toggleAllRow.get(0).getText()).contains("Включить все предметы");
        assertThat(toggleAllRow.get(0).getCallbackData()).isEqualTo(AutoHomeworkCallbackData.SPEC_TOGGLE_ALL);
    }

    @Test
    void buildSpecKeyboard_allSelected_showsExcludeAllButton() {
        var specs = List.of(
                new JournalSpecResponse(1L, "Math", "MTH"),
                new JournalSpecResponse(2L, "Physics", "PHY")
        );
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSpecKeyboard(specs, Set.of(1L, 2L));

        var toggleAllRow = kb.getKeyboard().get(0);
        assertThat(toggleAllRow.get(0).getText()).contains("Выключить все предметы");
        assertThat(toggleAllRow.get(0).getCallbackData()).isEqualTo(AutoHomeworkCallbackData.SPEC_TOGGLE_ALL);
    }

    @Test
    void buildSpecKeyboard_emptySpecs_showsIncludeAllButton() {
        InlineKeyboardMarkup kb = AutoHomeworkKeyboardFactory.buildSpecKeyboard(List.of(), Set.of());

        var toggleAllRow = kb.getKeyboard().get(0);
        assertThat(toggleAllRow.get(0).getText()).contains("Включить все предметы");
    }
}
