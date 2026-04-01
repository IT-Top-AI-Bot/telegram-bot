package com.aquadev.telegrambot.bot.service;

import com.aquadev.telegrambot.bot.exception.TelegramSendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TelegramMessageSenderTest {

    @Mock
    TelegramClient telegramClient;

    TelegramMessageSender sender;

    @BeforeEach
    void setUp() {
        sender = new TelegramMessageSender(telegramClient);
    }

    @Test
    void send_executesPlainTextMessage() throws TelegramApiException {
        sender.send(100L, "Hello");

        ArgumentCaptor<SendMessage> cap = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(cap.capture());
        assertThat(cap.getValue().getChatId()).isEqualTo("100");
        assertThat(cap.getValue().getText()).isEqualTo("Hello");
    }

    @Test
    void send_throwsTelegramSendException_onApiError() throws TelegramApiException {
        given(telegramClient.execute(any(SendMessage.class))).willThrow(new TelegramApiException("fail"));

        assertThatThrownBy(() -> sender.send(1L, "text"))
                .isInstanceOf(TelegramSendException.class);
    }

    @Test
    void sendHtml_executesMessageWithHtmlAndMarkup() throws TelegramApiException {
        var markup = InlineKeyboardMarkup.builder().build();
        sender.sendHtml(100L, "<b>Hi</b>", markup);

        ArgumentCaptor<SendMessage> cap = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(cap.capture());
        assertThat(cap.getValue().getParseMode()).isEqualTo("HTML");
        assertThat(cap.getValue().getText()).isEqualTo("<b>Hi</b>");
        assertThat(cap.getValue().getReplyMarkup()).isSameAs(markup);
    }

    @Test
    void editHtml_executesEditMessageText() throws TelegramApiException {
        var markup = InlineKeyboardMarkup.builder().build();
        sender.editHtml(100L, 5, "new text", markup);

        ArgumentCaptor<EditMessageText> cap = ArgumentCaptor.forClass(EditMessageText.class);
        verify(telegramClient).execute(cap.capture());
        assertThat(cap.getValue().getChatId()).isEqualTo("100");
        assertThat(cap.getValue().getMessageId()).isEqualTo(5);
        assertThat(cap.getValue().getText()).isEqualTo("new text");
        assertThat(cap.getValue().getParseMode()).isEqualTo("HTML");
    }

    @Test
    void editHtml_throwsTelegramSendException_onApiError() throws TelegramApiException {
        given(telegramClient.execute(any(EditMessageText.class))).willThrow(new TelegramApiException("err"));

        assertThatThrownBy(() -> sender.editHtml(1L, 1, "x", InlineKeyboardMarkup.builder().build()))
                .isInstanceOf(TelegramSendException.class);
    }

    @Test
    void editMarkup_executesEditMessageReplyMarkup() throws TelegramApiException {
        var markup = InlineKeyboardMarkup.builder().build();
        sender.editMarkup(100L, 7, markup);

        ArgumentCaptor<EditMessageReplyMarkup> cap = ArgumentCaptor.forClass(EditMessageReplyMarkup.class);
        verify(telegramClient).execute(cap.capture());
        assertThat(cap.getValue().getChatId()).isEqualTo("100");
        assertThat(cap.getValue().getMessageId()).isEqualTo(7);
    }

    @Test
    void editMarkup_throwsTelegramSendException_onApiError() throws TelegramApiException {
        given(telegramClient.execute(any(EditMessageReplyMarkup.class))).willThrow(new TelegramApiException("e"));

        assertThatThrownBy(() -> sender.editMarkup(1L, 1, InlineKeyboardMarkup.builder().build()))
                .isInstanceOf(TelegramSendException.class);
    }

    @Test
    void answerCallback_withoutText_executesWithoutText() throws TelegramApiException {
        sender.answerCallback("cb-1");

        ArgumentCaptor<AnswerCallbackQuery> cap = ArgumentCaptor.forClass(AnswerCallbackQuery.class);
        verify(telegramClient).execute(cap.capture());
        assertThat(cap.getValue().getCallbackQueryId()).isEqualTo("cb-1");
        assertThat(cap.getValue().getText()).isNull();
    }

    @Test
    void answerCallback_withText_executesWithTextAndNoAlert() throws TelegramApiException {
        sender.answerCallback("cb-1", "Done!");

        ArgumentCaptor<AnswerCallbackQuery> cap = ArgumentCaptor.forClass(AnswerCallbackQuery.class);
        verify(telegramClient).execute(cap.capture());
        assertThat(cap.getValue().getText()).isEqualTo("Done!");
        assertThat(cap.getValue().getShowAlert()).isFalse();
    }

    @Test
    void answerCallback_silentlyIgnoresApiError() throws TelegramApiException {
        given(telegramClient.execute(any(AnswerCallbackQuery.class))).willThrow(new TelegramApiException("e"));

        // should not throw
        sender.answerCallback("cb-1");
    }
}
