package com.aquadev.telegrambot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;


@Configuration
public class TelegramWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.addCustomConverter(new TelegramJackson2MessageConverter());
    }

    private static class TelegramJackson2MessageConverter extends AbstractHttpMessageConverter<Object> {

        private final ObjectMapper mapper = new ObjectMapper();

        TelegramJackson2MessageConverter() {
            super(MediaType.APPLICATION_JSON);
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            return clazz.getPackageName().startsWith("org.telegram.telegrambots.");
        }

        @Override
        protected @NonNull Object readInternal(@NonNull Class<?> clazz, HttpInputMessage inputMessage) throws IOException {
            return mapper.readValue(inputMessage.getBody(), clazz);
        }

        @Override
        protected void writeInternal(@NonNull Object value, HttpOutputMessage outputMessage) throws IOException {
            mapper.writeValue(outputMessage.getBody(), value);
        }
    }
}
