package ru.priyut.coordination.infrastructure.persistence.record;

import java.time.Instant;
import java.util.UUID;

/**
 * Record для маппинга ResultSet из таблицы trip_history.
 */
public record TripHistoryRecord(
        long id,
        UUID tripUuid,
        String eventType,
        Long actorTelegramId,
        String payload,
        Instant occurredAt
) {
}