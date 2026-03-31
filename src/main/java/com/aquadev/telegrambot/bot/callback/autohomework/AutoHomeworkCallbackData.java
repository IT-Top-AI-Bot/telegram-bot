package com.aquadev.telegrambot.bot.callback.autohomework;

public final class AutoHomeworkCallbackData {

    public static final String PREFIX = "autohw:";
    public static final String SET_ENABLED = PREFIX + "set_enabled:";
    public static final String OPEN_SPECS = PREFIX + "open_specs";
    public static final String SPEC_TOGGLE = PREFIX + "spec_toggle:";
    public static final String SPEC_TOGGLE_ALL = PREFIX + "spec_toggle_all";
    public static final String SPEC_SAVE = PREFIX + "spec_save";
    public static final String CANCEL = PREFIX + "cancel";

    private AutoHomeworkCallbackData() {
    }
}
