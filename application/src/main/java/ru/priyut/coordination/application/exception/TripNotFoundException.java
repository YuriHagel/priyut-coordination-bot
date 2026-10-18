package ru.priyut.coordination.application.exception;

import ru.priyut.coordination.domain.shared.DomainException;

/**
 * Поездка не найдена.
 */
public class TripNotFoundException extends DomainException {

    public TripNotFoundException(Long tripId) {
        super("Trip with ID " + tripId + " not found");
    }
}