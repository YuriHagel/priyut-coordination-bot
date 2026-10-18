package ru.priyut.coordination.infrastructure.persistence.record;

import java.time.Instant;

/**
 * Record для маппинга ResultSet из таблицы admin_whitelist.
 */
public record AdminWhitelistRecord(
        long telegramId,
        String displayName,
        Instant addedAt,
        Long addedBy
) {
}