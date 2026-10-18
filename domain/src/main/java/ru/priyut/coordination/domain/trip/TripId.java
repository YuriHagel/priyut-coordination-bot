package ru.priyut.coordination.domain.trip;

import ru.priyut.coordination.domain.shared.DomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Уникальный идентификатор поездки.
 */
public record TripId(UUID value) {

    public TripId {
        Objects.requireNonNull(value, "TripId value must not be null");
    }

    public static TripId of(UUID value) {
        return new TripId(value);
    }

    public static TripId generate() {
        return new TripId(UUID.randomUUID());
    }

    public static TripId parse(String value) {
        try {
            return new TripId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid TripId: " + value, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}