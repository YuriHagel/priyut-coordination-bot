package ru.priyut.coordination.domain.trip;

import ru.priyut.coordination.domain.shared.DomainException;
import ru.priyut.coordination.domain.volunteer.VolunteerId;

import java.time.Instant;
import java.util.Objects;

/**
 * Назначение волонтёра на поездку.
 * Хранит информацию о том, кто назначен и кем (админом).
 * Value object: неизменяемый.
 */
public record TripAssignment(
        VolunteerId volunteerId,
        Long assignedByAdminTelegramId,
        Instant assignedAt
) {

    public TripAssignment {
        Objects.requireNonNull(volunteerId, "volunteerId must not be null");
        Objects.requireNonNull(assignedByAdminTelegramId, "assignedByAdminTelegramId must not be null");
        Objects.requireNonNull(assignedAt, "assignedAt must not be null");

        if (assignedByAdminTelegramId <= 0) {
            throw new DomainException("Admin telegram ID must be positive");
        }
    }

    public static TripAssignment of(
            VolunteerId volunteerId,
            Long assignedByAdminTelegramId,
            Instant assignedAt
    ) {
        return new TripAssignment(volunteerId, assignedByAdminTelegramId, assignedAt);
    }
}