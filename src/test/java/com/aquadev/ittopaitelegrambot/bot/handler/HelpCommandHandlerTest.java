package com.aquadev.ittopaitelegrambot.bot.handler;

import com.aquadev.ittopaitelegrambot.bot.CommandRegistry;
import com.aquadev.ittopaitelegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.ittopaitelegrambot.bot.service.TelegramMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HelpCommandHandlerTest {

    @Mock
    TelegramMessageSender sender;
    @Mock
    CommandRegistry commandRegistry;

    HelpCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HelpCommandHandler(sender, commandRegistry);
    }

    @Test
    void handle_sendsFormattedCommandList() {
        @TelegramBotCommand(value = "/start", description = "Запустить бота")
        class FakeHandler implements CommandHandler {
            @Override
            public void handle(Update u) {
            }
        }
        var annotation = FakeHandler.class.getAnnotation(TelegramBotCommand.class);
        given(commandRegistry.getCommandMetadata()).willReturn(List.of(annotation));

        Update update = mockUpdate(999L);
        handler.handle(update);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(sender).send(eq(999L), textCaptor.capture());
        assertThat(textCaptor.getValue())
                .contains("/start")
                .contains("Запустить бота");
    }

    private Update mockUpdate(long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        given(update.getMessage()).willReturn(message);
        given(message.getChatId()).willReturn(chatId);
        return update;
    }
}
