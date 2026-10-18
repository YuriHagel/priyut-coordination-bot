package ru.priyut.coordination.domain.consent;

import ru.priyut.coordination.domain.shared.TelegramId;

import java.time.Instant;
import java.util.Objects;

/**
 * Факт выдачи согласия на обработку ПДн.
 * Value object: неизменяемый.
 */
public record ConsentGrant(
        TelegramId telegramId,
        ConsentVersion version,
        Instant grantedAt,
        String source
) {

    public ConsentGrant {
        Objects.requireNonNull(telegramId, "telegramId must not be null");
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(grantedAt, "grantedAt must not be null");
        Objects.requireNonNull(source, "source must not be null");

        source = source.trim();
        if (source.isEmpty()) {
            source = "TELEGRAM_BOT";
        }
    }

    public static ConsentGrant of(
            TelegramId telegramId,
            ConsentVersion version,
            Instant grantedAt,
            String source
    ) {
        return new ConsentGrant(telegramId, version, grantedAt, source);
    }
}