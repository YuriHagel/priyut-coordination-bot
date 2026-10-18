package ru.priyut.coordination.application.port.in;

import ru.priyut.coordination.domain.trip.Trip;

/**
 * Сценарий завершения поездки (ТЗ 4.6).
 * Двухэтапное завершение: волонтёр → админ.
 */
public interface CompleteTripUseCase {

    /**
     * Волонтёр отмечает поездку выполненной.
     *
     * @param tripId ID поездки
     * @return поездка со статусом PENDING_COMPLETION
     */
    Trip markCompletedByVolunteer(Long tripId);

    /**
     * Админ возвращает поездку на доработку.
     *
     * @param tripId ID поездки
     * @return поездка со статусом ASSIGNED
     */
    Trip returnForRework(Long tripId);

    /**
     * Админ закрывает поездку.
     *
     * @param tripId ID поездки
     * @param adminTelegramId ID админа
     * @return поездка со статусом COMPLETED
     */
    Trip closeByAdmin(Long tripId, Long adminTelegramId);
}