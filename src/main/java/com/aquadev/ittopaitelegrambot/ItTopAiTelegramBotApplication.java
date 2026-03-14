package com.aquadev.ittopaitelegrambot;

import com.aquadev.ittopaitelegrambot.config.TelegramRuntimeHints;
import org.springframework.aot.hint.annotation.ImportRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
@ImportRuntimeHints(TelegramRuntimeHints.class)
public class ItTopAiTelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItTopAiTelegramBotApplication.class, args);
    }

}
