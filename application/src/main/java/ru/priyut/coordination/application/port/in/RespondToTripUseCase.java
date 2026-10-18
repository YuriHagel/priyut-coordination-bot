package ru.priyut.coordination.application.port.in;

import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.domain.volunteer.VolunteerId;

/**
 * Сценарий отклика волонтёра на поездку (ТЗ 4.3).
 */
public interface RespondToTripUseCase {

    /**
     * Отклик волонтёра на поездку.
     *
     * @param tripId ID поездки
     * @param volunteerId ID волонтёра
     * @return поездка со статусом VOLUNTEER_PENDING
     */
    Trip respond(Long tripId, VolunteerId volunteerId);
}