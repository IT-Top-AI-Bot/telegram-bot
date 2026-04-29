package com.aquadev.telegrambot.bot.callback.schedule;

import java.time.LocalDate;

public final class ScheduleCallbackData {

    public static final String WEEK = "schedule:week:";

    private ScheduleCallbackData() {
    }

    public static String weekOf(LocalDate mondayDate) {
        return WEEK + mondayDate;
    }
}
