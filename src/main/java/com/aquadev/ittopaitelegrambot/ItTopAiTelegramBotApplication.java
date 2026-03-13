package com.aquadev.ittopaitelegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ItTopAiTelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItTopAiTelegramBotApplication.class, args);
    }

}
