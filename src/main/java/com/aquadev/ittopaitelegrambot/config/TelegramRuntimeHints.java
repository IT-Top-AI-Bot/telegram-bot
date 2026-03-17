package com.aquadev.ittopaitelegrambot.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;


public class TelegramRuntimeHints implements RuntimeHintsRegistrar {

    private static final String[] SCAN_PATTERNS = {
            "classpath*:org/telegram/telegrambots/**/*.class",
            "classpath*:com/aquadev/ittopaitelegrambot/client/dto/**/*.class"
    };

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var resolver = new PathMatchingResourcePatternResolver();
        var metadataFactory = new CachingMetadataReaderFactory(resolver);
        for (String pattern : SCAN_PATTERNS) {
            try {
                for (Resource resource : resolver.getResources(pattern)) {
                    try {
                        String className = metadataFactory.getMetadataReader(resource)
                                .getClassMetadata().getClassName();
                        Class<?> clazz = classLoader.loadClass(className);
                        hints.reflection().registerType(clazz,
                                MemberCategory.ACCESS_DECLARED_FIELDS,
                                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                                MemberCategory.INVOKE_DECLARED_METHODS);
                    } catch (Exception _) {
                    }
                }
            } catch (Exception _) {
            }
        }
    }
}
