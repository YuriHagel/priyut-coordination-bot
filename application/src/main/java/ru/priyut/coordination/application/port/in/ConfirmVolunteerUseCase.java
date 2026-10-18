package ru.priyut.coordination.application.port.in;

import ru.priyut.coordination.domain.trip.Trip;

/**
 * Сценарий подтверждения волонтёра администратором (ТЗ 4.3).
 */
public interface ConfirmVolunteerUseCase {

    /**
     * Подтверждение волонтёра администратором.
     *
     * @param tripId ID поездки
     * @param adminTelegramId ID админа, подтверждающего
     * @return поездка со статусом ASSIGNED
     */
    Trip confirm(Long tripId, Long adminTelegramId);

    /**
     * Отклонение волонтёра администратором.
     *
     * @param tripId ID поездки
     * @param adminTelegramId ID админа, отклоняющего
     * @return поездка со статусом PUBLISHED (снова доступна для откликов)
     */
    Trip reject(Long tripId, Long adminTelegramId);
}