package com.aquadev.ittopaitelegrambot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.List;

/**
 * Registers a Jackson 2 message converter with highest priority for all
 * org.telegram.telegrambots.* types.
 * <p>
 * Spring Boot 4 uses Jackson 3 (tools.jackson) as primary ObjectMapper, but
 * telegrambots uses Jackson 2 annotations (@JsonDeserialize, @JsonProperty, etc.)
 * from com.fasterxml.jackson. Jackson 3 does not process Jackson 2 annotations,
 * so without this converter deserialization of Update/Message/User/etc. fails
 * with "no Creators" error.
 */
@Configuration
public class TelegramWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new TelegramJackson2MessageConverter());
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
        protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException {
            return mapper.readValue(inputMessage.getBody(), clazz);
        }

        @Override
        protected void writeInternal(Object value, HttpOutputMessage outputMessage) throws IOException {
            mapper.writeValue(outputMessage.getBody(), value);
        }
    }
}
