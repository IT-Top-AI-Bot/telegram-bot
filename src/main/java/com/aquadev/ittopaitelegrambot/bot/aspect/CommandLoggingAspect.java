package com.aquadev.ittopaitelegrambot.bot.aspect;

import com.aquadev.ittopaitelegrambot.bot.exception.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CommandLoggingAspect {

    private final GlobalExceptionHandler exceptionHandler;

    @Around("execution(* com.aquadev.ittopaitelegrambot.bot.handler.CommandHandler.handle(..))")
    public Object logCommand(ProceedingJoinPoint joinPoint) throws Throwable {
        Update update = (Update) joinPoint.getArgs()[0];
        String command = update.getMessage().getText().split("\\s+")[0].split("@")[0];
        User from = update.getMessage().getFrom();
        long userId = from.getId();
        String username = from.getUserName() != null ? "@" + from.getUserName() : from.getFirstName();

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            log.info("Command [{}] from {} (id={}) executed in {} ms",
                    command, username, userId, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable e) {
            log.error("Command [{}] from {} (id={}) failed in {} ms: {}",
                    command, username, userId, System.currentTimeMillis() - start, e.getMessage());
            exceptionHandler.handle(update, e);
            return null;
        }
    }
}
