package com.aquadev.ittopaitelegrambot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@Configuration
public class EndpointDocConfig {

    private final RequestMappingHandlerMapping handlerMapping;

    public EndpointDocConfig(@org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Bean
    public ApplicationRunner logEndpoints() {
        return args -> {
            log.info("Registered API Endpoints:");
            handlerMapping.getHandlerMethods().forEach((info, method) -> {
                log.info("Mapping: {} -> {}", info, method);
            });
        };
    }
}
