package ru.priyut.coordination.application.port.outbound;

import ru.priyut.coordination.domain.trip.TripId;

import java.util.List;

/**
 * Порт для работы с историей поездок.
 */
public interface TripHistoryRepository {

    void saveEvent(TripId tripId, String eventType, Long actorTelegramId, String payload);

    List<String> findEventsByTripId(TripId tripId);
}