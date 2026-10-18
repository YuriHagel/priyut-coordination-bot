package ru.priyut.coordination.domain.consent;

import ru.priyut.coordination.domain.shared.TelegramId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Запись в журнале аудита действий с согласием на ПДн (152-ФЗ).
 * Value object: неизменяемый.
 */
public record ConsentAuditEntry(
        UUID id,
        TelegramId telegramId,
        AuditAction action,
        ConsentVersion version,
        Instant occurredAt,
        String source,
        String comment
) {

    public enum AuditAction {
        GRANTED,
        WITHDRAWN,
        PROFILE_DELETION_REQUESTED,
        PROFILE_DELETED,
        PROFILE_ARCHIVED
    }

    public ConsentAuditEntry {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(telegramId, "telegramId must not be null");
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(source, "source must not be null");

        comment = comment == null ? "" : comment.trim();
        source = source.trim();
    }

    public static ConsentAuditEntry of(
            TelegramId telegramId,
            AuditAction action,
            ConsentVersion version,
            Instant occurredAt,
            String source,
            String comment
    ) {
        return new ConsentAuditEntry(
                UUID.randomUUID(), telegramId, action, version, occurredAt, source, comment
        );
    }

    public static ConsentAuditEntry grant(
            TelegramId telegramId, ConsentVersion version, Instant occurredAt, String source
    ) {
        return of(telegramId, AuditAction.GRANTED, version, occurredAt, source, null);
    }

    public static ConsentAuditEntry withdraw(
            TelegramId telegramId, ConsentVersion version, Instant occurredAt, String source, String reason
    ) {
        return of(telegramId, AuditAction.WITHDRAWN, version, occurredAt, source, reason);
    }
}