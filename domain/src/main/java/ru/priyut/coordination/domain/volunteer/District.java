package ru.priyut.coordination.domain.volunteer;

import ru.priyut.coordination.domain.shared.DomainException;

import java.util.Objects;

/**
 * Район проживания волонтёра.
 * Value object: неизменяемый, с валидацией.
 */
public record District(String name) {

    // Предустановленные районы (из mockups ТЗ)
    public static final District CENTRAL = new District("Центральный");
    public static final District MOSCOWSKY = new District("Московский");
    public static final District VSEVOLOZHSKY = new District("Всеволожский");

    public District {
        Objects.requireNonNull(name, "District name must not be null");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new DomainException("District name must not be empty");
        }
        if (trimmed.length() > 100) {
            throw new DomainException("District name is too long: " + trimmed.length());
        }
        name = trimmed;
    }

    public static District of(String name) {
        return new District(name);
    }

    @Override
    public String toString() {
        return name;
    }
}