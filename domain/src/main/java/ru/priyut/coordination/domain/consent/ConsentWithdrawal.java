package ru.priyut.coordination.domain.consent;

import ru.priyut.coordination.domain.shared.TelegramId;

import java.time.Instant;
import java.util.Objects;

/**
 * Факт отзыва согласия на обработку ПДн.
 * Value object: неизменяемый.
 */
public record ConsentWithdrawal(
        TelegramId telegramId,
        Instant withdrawnAt,
        String reason,
        String source
) {

    public ConsentWithdrawal {
        Objects.requireNonNull(telegramId, "telegramId must not be null");
        Objects.requireNonNull(withdrawnAt, "withdrawnAt must not be null");
        Objects.requireNonNull(source, "source must not be null");

        reason = reason == null ? "" : reason.trim();
        source = source.trim();
        if (source.isEmpty()) {
            source = "TELEGRAM_BOT";
        }
    }

    public static ConsentWithdrawal of(
            TelegramId telegramId,
            Instant withdrawnAt,
            String reason,
            String source
    ) {
        return new ConsentWithdrawal(telegramId, withdrawnAt, reason, source);
    }
}