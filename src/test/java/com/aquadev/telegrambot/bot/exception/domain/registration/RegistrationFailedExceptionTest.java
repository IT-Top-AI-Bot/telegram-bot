package com.aquadev.telegrambot.bot.exception.domain.registration;

import com.aquadev.telegrambot.bot.exception.base.BotException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationFailedExceptionTest {

    @Test
    void isABotExceptionWithNonNullMessage() {
        var ex = new RegistrationFailedException();

        assertThat(ex).isInstanceOf(BotException.class);
        assertThat(ex.getMessage()).isNotBlank();
    }
}

