package ru.priyut.coordination.domain.trip;

import ru.priyut.coordination.domain.shared.DomainException;

import java.util.Objects;

/**
 * Маршрут поездки: откуда → куда + пункт назначения.
 * Value object: неизменяемый, с валидацией.
 */
public record Route(String from, String to, String destination) {

    public Route {
        Objects.requireNonNull(from, "Route 'from' must not be null");
        Objects.requireNonNull(to, "Route 'to' must not be null");
        Objects.requireNonNull(destination, "Route 'destination' must not be null");

        from = from.trim();
        to = to.trim();
        destination = destination.trim();

        if (from.isEmpty()) {
            throw new DomainException("Route 'from' must not be empty");
        }
        if (to.isEmpty()) {
            throw new DomainException("Route 'to' must not be empty");
        }
        if (destination.isEmpty()) {
            throw new DomainException("Route 'destination' must not be empty");
        }
        if (from.length() > 200 || to.length() > 200 || destination.length() > 200) {
            throw new DomainException("Route fields are too long");
        }
    }

    public static Route of(String from, String to, String destination) {
        return new Route(from, to, destination);
    }

    /**
     * Форматированное представление: "Приют → ВетКлиника «Друг», ул. Ленина 5"
     */
    public String formatted() {
        return from + " → " + destination;
    }

    @Override
    public String toString() {
        return formatted();
    }
}