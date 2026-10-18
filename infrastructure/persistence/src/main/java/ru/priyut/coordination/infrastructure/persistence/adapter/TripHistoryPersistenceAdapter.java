package ru.priyut.coordination.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import ru.priyut.coordination.application.port.outbound.TripHistoryRepository;
import ru.priyut.coordination.domain.trip.TripId;
import ru.priyut.coordination.infrastructure.persistence.repository.JdbcTripHistoryRepository;

import java.util.List;

@Component
public class TripHistoryPersistenceAdapter implements TripHistoryRepository {

    private final JdbcTripHistoryRepository jdbcRepository;

    public TripHistoryPersistenceAdapter(JdbcTripHistoryRepository jdbcRepository) {
        this.jdbcRepository = jdbcRepository;
    }

    @Override
    public void saveEvent(TripId tripId, String eventType, Long actorTelegramId, String payload) {
        jdbcRepository.saveEvent(tripId, eventType, actorTelegramId, payload);
    }

    @Override
    public List<String> findEventsByTripId(TripId tripId) {
        return jdbcRepository.findEventsByTripId(tripId)
                .stream()
                .map(record -> record.eventType() + " at " + record.occurredAt())
                .toList();
    }
}