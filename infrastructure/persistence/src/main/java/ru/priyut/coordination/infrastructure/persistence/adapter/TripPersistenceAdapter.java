package ru.priyut.coordination.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import ru.priyut.coordination.application.port.outbound.TripRepository;
import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.domain.trip.TripStatus;
import ru.priyut.coordination.domain.volunteer.VolunteerId;
import ru.priyut.coordination.infrastructure.persistence.repository.JdbcTripRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class TripPersistenceAdapter implements TripRepository {

    private final JdbcTripRepository jdbcRepository;

    public TripPersistenceAdapter(JdbcTripRepository jdbcRepository) {
        this.jdbcRepository = jdbcRepository;
    }

    @Override
    public Optional<Trip> findById(Long id) {
        return jdbcRepository.findById(id);
    }

    @Override
    public List<Trip> findByStatus(TripStatus status) {
        return jdbcRepository.findByStatus(status);
    }

    @Override
    public List<Trip> findUpcomingTrips(LocalDateTime from, LocalDateTime to) {
        return jdbcRepository.findUpcomingTrips(from, to);
    }

    @Override
    public List<Trip> findByAssignedVolunteerId(VolunteerId volunteerId) {
        return jdbcRepository.findByAssignedVolunteerId(volunteerId);
    }

    @Override
    public Trip save(Trip trip) {
        return jdbcRepository.save(trip);
    }

    @Override
    public void deleteById(Long id) {
        jdbcRepository.deleteById(id);
    }
}