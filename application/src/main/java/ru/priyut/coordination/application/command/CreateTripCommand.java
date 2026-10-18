package ru.priyut.coordination.application.command;

import ru.priyut.coordination.domain.trip.Route;
import ru.priyut.coordination.domain.trip.TripComment;
import ru.priyut.coordination.domain.trip.TripType;

import java.time.LocalDateTime;

/**
 * Команда для создания поездки.
 */
public record CreateTripCommand(
        Long adminTelegramId,
        Route route,
        LocalDateTime tripDateTime,
        String taskDescription,
        TripType type,
        TripComment comment
) {
}