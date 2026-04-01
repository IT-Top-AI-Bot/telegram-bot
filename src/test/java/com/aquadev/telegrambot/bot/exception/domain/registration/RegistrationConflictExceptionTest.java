package com.aquadev.telegrambot.bot.exception.domain.registration;

import com.aquadev.telegrambot.bot.exception.base.BotException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationConflictExceptionTest {

    @Test
    void messageContainsUsername() {
        var ex = new RegistrationConflictException("johndoe");

        assertThat(ex).isInstanceOf(BotException.class);
        assertThat(ex.getMessage()).contains("johndoe");
    }
}

