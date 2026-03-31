package com.aquadev.telegrambot.bot.aspect;

import com.aquadev.telegrambot.bot.exception.GlobalExceptionHandler;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
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
    private final ObservationRegistry observationRegistry;

    @Around("execution(* com.aquadev.telegrambot.bot.handler.CommandHandler.handle(..))")
    public Object logCommand(ProceedingJoinPoint joinPoint) throws Throwable {
        Update update = (Update) joinPoint.getArgs()[0];
        String command = update.getMessage().getText().split("\\s+")[0].split("@")[0];
        User from = update.getMessage().getFrom();
        long userId = from.getId();
        String username = from.getUserName() != null ? "@" + from.getUserName() : from.getFirstName();

        long start = System.currentTimeMillis();
        Observation observation = Observation.createNotStarted("telegram.bot.command", observationRegistry)
                .contextualName(command)
                .lowCardinalityKeyValue("command", command)
                .highCardinalityKeyValue("user.id", String.valueOf(userId))
                .start();

        try (Observation.Scope ignored = observation.openScope()) {
            Object result = joinPoint.proceed();
            log.info("Command [{}] from {} (id={}) executed in {} ms",
                    command, username, userId, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable e) {
            observation.error(e);
            log.error("Command [{}] from {} (id={}) failed in {} ms: {}",
                    command, username, userId, System.currentTimeMillis() - start, e.getMessage());
            exceptionHandler.handle(update, e);
            return null;
        } finally {
            observation.stop();
        }
    }
}
