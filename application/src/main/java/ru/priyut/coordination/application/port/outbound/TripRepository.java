package ru.priyut.coordination.application.port.outbound;

import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.domain.trip.TripStatus;
import ru.priyut.coordination.domain.volunteer.VolunteerId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Порт для работы с поездками.
 */
public interface TripRepository {

    Optional<Trip> findById(Long id);

    List<Trip> findByStatus(TripStatus status);

    List<Trip> findUpcomingTrips(LocalDateTime from, LocalDateTime to);

    List<Trip> findByAssignedVolunteerId(VolunteerId volunteerId);

    Trip save(Trip trip);

    void deleteById(Long id);
}