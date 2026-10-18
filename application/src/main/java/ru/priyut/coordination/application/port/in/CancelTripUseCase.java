package ru.priyut.coordination.application.port.in;

import ru.priyut.coordination.application.command.CancelTripCommand;
import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.domain.volunteer.VolunteerId;

/**
 * Сценарий отмены поездки (ТЗ 4.4).
 */
public interface CancelTripUseCase {

    /**
     * Отмена поездки волонтёром.
     *
     * @param command команда отмены с причиной
     * @return поездка со статусом PUBLISHED (ищет нового волонтёра)
     */
    Trip cancelByVolunteer(CancelTripCommand command);

    /**
     * Отмена поездки администратором.
     *
     * @param tripId ID поездки
     * @param adminTelegramId ID админа
     * @param reason причина отмены
     * @return поездка со статусом CANCELLED
     */
    Trip cancelByAdmin(Long tripId, Long adminTelegramId, String reason);
}