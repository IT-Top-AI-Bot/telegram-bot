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

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CallbackTracingAspect {

    private final ObservationRegistry observationRegistry;
    private final GlobalExceptionHandler exceptionHandler;

    @Around("execution(* com.aquadev.telegrambot.bot.callback.CallbackHandler.handle(..))")
    public Object traceCallback(ProceedingJoinPoint joinPoint) throws Throwable {
        Update update = (Update) joinPoint.getArgs()[0];
        String callbackData = update.getCallbackQuery().getData();
        String action = callbackData != null ? callbackData.split(":")[0] : "unknown";
        long userId = update.getCallbackQuery().getFrom().getId();
        String handlerName = joinPoint.getTarget().getClass().getSimpleName();

        long start = System.currentTimeMillis();
        Observation observation = Observation.createNotStarted("telegram.bot.callback", observationRegistry)
                .contextualName("callback " + action)
                .lowCardinalityKeyValue("callback.action", action)
                .lowCardinalityKeyValue("handler", handlerName)
                .highCardinalityKeyValue("user.id", String.valueOf(userId))
                .start();

        try (Observation.Scope ignored = observation.openScope()) {
            Object result = joinPoint.proceed();
            log.info("Callback [{}] from user {} ({}) in {} ms",
                    callbackData, userId, handlerName, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable e) {
            observation.error(e);
            log.error("Callback [{}] from user {} ({}) failed in {} ms: {}",
                    callbackData, userId, handlerName, System.currentTimeMillis() - start, e.getMessage());
            exceptionHandler.handle(update, e);
            return null;
        } finally {
            observation.stop();
        }
    }
}
