package com.aquadev.telegrambot.bot.exception;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramSendExceptionTest {

    @Test
    void constructorStoresMessageAndCause() {
        var cause = new TelegramApiException("api error");
        var ex = new TelegramSendException("send failed", cause);

        assertThat(ex.getMessage()).isEqualTo("send failed");
        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex.getTelegramCause()).isSameAs(cause);
    }
}

