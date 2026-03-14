package com.aquadev.ittopaitelegrambot.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

/**
 * Registers reflection hints for Telegram Bot API model classes.
 * Required for GraalVM/Azul NIK native image — Jackson deserializes these
 * via reflection at runtime (WebhookController.telegramObjectMapper).
 */
public class TelegramRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver(classLoader);
        try {
            Resource[] resources = resolver.getResources(
                    "classpath*:org/telegram/telegrambots/meta/api/**/*.class");
            for (Resource resource : resources) {
                String urlPath = resource.getURL().getPath();
                int idx = urlPath.indexOf("org/telegram");
                if (idx == -1) continue;
                String className = urlPath.substring(idx)
                        .replace('/', '.')
                        .replace(".class", "");
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    hints.reflection().registerType(clazz,
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.DECLARED_FIELDS,
                            MemberCategory.INVOKE_PUBLIC_METHODS);
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                    // skip classes that can't be loaded in this context
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to scan Telegram API classes for native image hints", e);
        }
    }
}
