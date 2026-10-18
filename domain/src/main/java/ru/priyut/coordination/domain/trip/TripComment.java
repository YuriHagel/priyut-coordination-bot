package ru.priyut.coordination.domain.trip;

import ru.priyut.coordination.domain.shared.DomainException;

/**
 * Комментарий к поездке.
 * Value object: неизменяемый, с валидацией длины.
 */
public record TripComment(String value) {

    public static final int MAX_LENGTH = 500;

    public TripComment {
        if (value == null) {
            value = "";
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new DomainException(
                    "Trip comment is too long: " + value.length() + " (max " + MAX_LENGTH + ")");
        }
    }

    public static TripComment of(String value) {
        return new TripComment(value);
    }

    public static TripComment empty() {
        return new TripComment("");
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public String toString() {
        return value;
    }
}