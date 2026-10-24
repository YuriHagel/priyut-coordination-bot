package ru.priyut.coordination.application.command;

import ru.priyut.coordination.domain.volunteer.VolunteerId;

/**
 * Команда для отмены поездки волонтёром.
 */
public record CancelTripCommand(
        Long tripId,
        VolunteerId volunteerId,
        String reason
) {
}