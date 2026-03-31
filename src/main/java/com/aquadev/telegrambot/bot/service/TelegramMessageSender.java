package com.aquadev.telegrambot.bot.service;

import com.aquadev.telegrambot.bot.exception.TelegramSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageSender {

    private final TelegramClient telegramClient;

    public void send(long chatId, String text) {
        execute(SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build());
    }

    public void sendHtml(long chatId, String text, InlineKeyboardMarkup markup) {
        execute(SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(markup)
                .build());
    }

    public void editHtml(long chatId, int messageId, String text, InlineKeyboardMarkup markup) {
        try {
            telegramClient.execute(EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(text)
                    .parseMode("HTML")
                    .replyMarkup(markup)
                    .build());
        } catch (TelegramApiException e) {
            throw new TelegramSendException("Не удалось обновить сообщение", e);
        }
    }

    public void editMarkup(long chatId, int messageId, InlineKeyboardMarkup markup) {
        try {
            telegramClient.execute(EditMessageReplyMarkup.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .replyMarkup(markup)
                    .build());
        } catch (TelegramApiException e) {
            throw new TelegramSendException("Не удалось обновить клавиатуру", e);
        }
    }

    public void answerCallback(String callbackId) {
        answerCallback(callbackId, null);
    }

    public void answerCallback(String callbackId, String text) {
        try {
            var builder = AnswerCallbackQuery.builder().callbackQueryId(callbackId);
            if (text != null) builder.text(text).showAlert(false);
            telegramClient.execute(builder.build());
        } catch (TelegramApiException e) {
            log.warn("Не удалось ответить на callback {}: {}", callbackId, e.getMessage());
        }
    }

    public void answerCallbackAlert(String callbackId, String text) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackId)
                    .text(text)
                    .showAlert(true)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не удалось показать alert для callback {}: {}", callbackId, e.getMessage());
        }
    }

    public void deleteMessage(long chatId, int messageId) {
        try {
            telegramClient.execute(DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не удалось удалить сообщение chatId={}, messageId={}: {}", chatId, messageId, e.getMessage());
        }
    }

    private void execute(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new TelegramSendException("Не удалось отправить сообщение", e);
        }
    }
}
