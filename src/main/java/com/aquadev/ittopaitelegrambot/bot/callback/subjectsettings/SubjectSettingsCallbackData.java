package com.aquadev.ittopaitelegrambot.bot.callback.subjectsettings;

public final class SubjectSettingsCallbackData {

    public static final String PREFIX = "subj:";
    public static final String OPEN = PREFIX + "open";
    public static final String SPEC = PREFIX + "spec:";           // + specId
    public static final String SET_PROMPT = PREFIX + "set_prompt:"; // + specId
    public static final String SET_STATIC = PREFIX + "set_static:"; // + specId
    public static final String DEL_PROMPT = PREFIX + "del_prompt:";   // + specId
    public static final String DEL_STATIC = PREFIX + "del_static:";   // + specId
    public static final String VIEW_PROMPT = PREFIX + "view_prompt:"; // + specId
    public static final String VIEW_STATIC = PREFIX + "view_static:"; // + specId
    public static final String CANCEL_INPUT = PREFIX + "cancel_input";

    private SubjectSettingsCallbackData() {
    }
}
