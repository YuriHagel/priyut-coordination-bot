package ru.priyut.coordination.domain.volunteer;

import ru.priyut.coordination.domain.shared.DomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Внутренний идентификатор волонтёра в системе.
 */
public record VolunteerId(UUID value) {

    public VolunteerId {
        Objects.requireNonNull(value, "VolunteerId value must not be null");
    }

    public static VolunteerId of(UUID value) {
        return new VolunteerId(value);
    }

    public static VolunteerId generate() {
        return new VolunteerId(UUID.randomUUID());
    }

    public static VolunteerId parse(String value) {
        try {
            return new VolunteerId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid VolunteerId: " + value, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}