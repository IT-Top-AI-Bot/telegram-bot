package com.aquadev.ittopaitelegrambot.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;

public class TelegramRuntimeHints implements RuntimeHintsRegistrar {

    private static final MemberCategory[] ALL_MEMBER_CATEGORIES = {
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS
    };

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Scans ALL .class files including inner builder classes (e.g. ApiResponse$ApiResponseBuilder)
        // which ClassPathScanningCandidateComponentProvider misses
        registerByPattern(hints, classLoader, "classpath*:org/telegram/telegrambots/**/*.class");
    }

    private void registerByPattern(RuntimeHints hints, ClassLoader classLoader, String pattern) {
        var resolver = new PathMatchingResourcePatternResolver();
        var metadataFactory = new CachingMetadataReaderFactory(resolver);
        try {
            for (Resource resource : resolver.getResources(pattern)) {
                try {
                    String className = metadataFactory.getMetadataReader(resource)
                            .getClassMetadata().getClassName();
                    hints.reflection().registerTypeIfPresent(classLoader, className, ALL_MEMBER_CATEGORIES);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }
}
