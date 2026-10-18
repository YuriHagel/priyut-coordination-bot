package ru.priyut.coordination.infrastructure.persistence.record;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record для маппинга ResultSet из таблицы trips.
 */
public record TripRecord(
        long id,
        UUID uuid,
        long createdByAdminTelegramId,
        String routeFrom,
        String routeTo,
        String routeDestination,
        LocalDateTime tripDateTime,
        String taskDescription,
        String tripType,
        String comment,
        String status,
        UUID assignedVolunteerUuid,
        Long assignedByAdminTelegramId,
        Instant assignedAt,
        Instant publishedAt,
        Instant cancelledAt,
        String cancelReason,
        Instant completedAt,
        Long closedByAdminTelegramId,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}