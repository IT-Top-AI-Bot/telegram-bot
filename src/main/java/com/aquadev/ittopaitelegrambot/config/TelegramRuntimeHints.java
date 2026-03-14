package com.aquadev.ittopaitelegrambot.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;

import java.lang.reflect.Field;

public class TelegramRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var resolver = new PathMatchingResourcePatternResolver();
        var metadataFactory = new CachingMetadataReaderFactory(resolver);
        try {
            for (Resource resource : resolver.getResources("classpath*:org/telegram/telegrambots/**/*.class")) {
                try {
                    String className = metadataFactory.getMetadataReader(resource)
                            .getClassMetadata().getClassName();
                    Class<?> clazz = classLoader.loadClass(className);

                    // Constructors and methods for Jackson instantiation and property access
                    hints.reflection().registerType(clazz,
                            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            MemberCategory.INVOKE_DECLARED_METHODS);

                    // Register each field explicitly — replacement for deprecated DECLARED_FIELDS in Spring 7
                    for (Field field : clazz.getDeclaredFields()) {
                        hints.reflection().registerField(field);
                    }
                } catch (Exception _) {
                }
            }
        } catch (Exception _) {
        }
    }
}
