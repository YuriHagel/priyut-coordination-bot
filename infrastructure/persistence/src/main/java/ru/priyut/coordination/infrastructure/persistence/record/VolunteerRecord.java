package ru.priyut.coordination.infrastructure.persistence.record;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Record для маппинга ResultSet из таблицы volunteers.
 */
public record VolunteerRecord(
        UUID id,
        long telegramId,
        String fullName,
        String phone,
        String district,
        String carBrand,
        String carModel,
        String carColor,
        String availabilityDays, // MONDAY,TUESDAY,WEDNESDAY
        LocalTime availabilityTimeFrom,
        LocalTime availabilityTimeTo,
        String availabilityStatus,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Instant lastActivityAt
) {
}