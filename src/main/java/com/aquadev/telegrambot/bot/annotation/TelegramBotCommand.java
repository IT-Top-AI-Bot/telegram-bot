package com.aquadev.telegrambot.bot.annotation;

import com.aquadev.telegrambot.client.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TelegramBotCommand {
    String value();

    String description();

    UserRole[] roles() default {UserRole.USER};
}
