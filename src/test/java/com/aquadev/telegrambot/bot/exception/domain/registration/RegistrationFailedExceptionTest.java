package com.aquadev.telegrambot.bot.exception.domain.registration;

import com.aquadev.telegrambot.bot.exception.base.BotException;
import org.junit.jupiter.api.Test;

class RegistrationFailedExceptionTest {

    @Test
    void isABotExceptionWithNonNullMessage() {
        var ex = new RegistrationFailedException();

        assertThat(ex).isInstanceOf(BotException.class);
        assertThat(ex.getMessage()).isNotBlank();
    }
}
