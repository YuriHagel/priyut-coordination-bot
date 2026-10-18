package ru.priyut.coordination.application.port.in;

import ru.priyut.coordination.application.command.CreateTripCommand;
import ru.priyut.coordination.domain.trip.Trip;

/**
 * Сценарий создания поездки администратором (ТЗ 4.2).
 */
public interface CreateTripUseCase {

    /**
     * Создание черновика поездки.
     *
     * @param command параметры поездки
     * @return созданная поездка со статусом DRAFT
     */
    Trip createDraft(CreateTripCommand command);

    /**
     * Публикация поездки (переход DRAFT → PUBLISHED).
     *
     * @param tripId ID поездки
     * @param adminTelegramId ID админа, публикующего поездку
     * @return опубликованная поездка
     */
    Trip publish(Long tripId, Long adminTelegramId);
}