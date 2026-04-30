package com.aquadev.telegrambot.bot.handler;

import com.aquadev.telegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.telegrambot.bot.service.TelegramMessageSender;
import com.aquadev.telegrambot.client.JournalClient;
import com.aquadev.telegrambot.client.dto.JournalGamePointResponse;
import com.aquadev.telegrambot.client.dto.JournalGroupInfoResponse;
import com.aquadev.telegrambot.client.dto.JournalProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@TelegramBotCommand(value = "/me", description = "Моя информация из журнала")
public class MeCommandHandler implements CommandHandler {

    private static final DateTimeFormatter BIRTHDAY_FMT =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"));

    private final JournalClient journalClient;
    private final TelegramMessageSender sender;

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long telegramUserId = update.getMessage().getFrom().getId();

        JournalProfileResponse profile = journalClient.getProfile(telegramUserId);
        String text = formatProfile(profile);
        if (profile.photo() != null && !profile.photo().isBlank()) {
            sender.sendPhotoHtml(chatId, profile.photo(), text);
        } else {
            sender.sendHtml(chatId, text);
        }
    }

    private String formatProfile(JournalProfileResponse p) {
        StringBuilder sb = new StringBuilder();

        sb.append("👤 <b>").append(p.fullName()).append("</b>\n\n");

        String groupName = primaryGroupName(p.groups());
        if (groupName != null) {
            sb.append("🎓 Группа: ").append(groupName).append("\n");
        }
        if (p.streamName() != null) {
            sb.append("🌊 Поток: ").append(p.streamName()).append("\n");
        }
        List<JournalGamePointResponse> gp = p.gamingPoints();
        int money = pointsByType(gp, 1);
        int gems = pointsByType(gp, 2);
        int total = money + gems;
        if (total > 0) {
            sb.append("\n💰 Топкоины: ").append(formatPoints(money)).append("\n");
            sb.append("💎 Топгемы: ").append(formatPoints(gems)).append("\n");
            sb.append("📦 Итого: ").append(formatPoints(total)).append("\n");
        }
        if (p.birthday() != null) {
            sb.append("\n🎂 ").append(p.birthday().format(BIRTHDAY_FMT));
            if (p.age() != null) {
                sb.append(" · ").append(p.age()).append(" лет");
            }
            sb.append("\n");
        }

        return sb.toString().stripTrailing();
    }

    private String primaryGroupName(List<JournalGroupInfoResponse> groups) {
        if (groups == null || groups.isEmpty()) return null;
        return groups.stream()
                .filter(g -> Boolean.TRUE.equals(g.isPrimary()))
                .map(JournalGroupInfoResponse::name)
                .findFirst()
                .orElse(groups.get(0).name());
    }

    private int pointsByType(List<JournalGamePointResponse> points, int typeId) {
        if (points == null) return 0;
        return points.stream()
                .filter(p -> p.typeId() != null && p.typeId() == typeId)
                .mapToInt(p -> p.points() != null ? p.points() : 0)
                .sum();
    }

    private String formatPoints(int points) {
        if (points >= 1000) {
            return String.format("%,d", points).replace(',', ' ');
        }
        return String.valueOf(points);
    }
}
