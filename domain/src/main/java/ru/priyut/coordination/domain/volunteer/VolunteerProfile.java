package ru.priyut.coordination.domain.volunteer;

import ru.priyut.coordination.domain.shared.DomainException;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.util.Objects;

/**
 * Данные анкеты волонтёра (без ID и статуса).
 * Value object: неизменяемый, с валидацией.
 */
public record VolunteerProfile(
        TelegramId telegramId,
        String fullName,
        PhoneNumber phone,
        District district,
        Car car,
        Availability availability
) {

    public VolunteerProfile {
        Objects.requireNonNull(telegramId, "telegramId must not be null");
        Objects.requireNonNull(fullName, "fullName must not be null");
        Objects.requireNonNull(phone, "phone must not be null");
        Objects.requireNonNull(district, "district must not be null");
        Objects.requireNonNull(car, "car must not be null");
        Objects.requireNonNull(availability, "availability must not be null");

        fullName = fullName.trim();
        if (fullName.isEmpty()) {
            throw new DomainException("Full name must not be empty");
        }
        if (fullName.length() > 100) {
            throw new DomainException("Full name is too long: " + fullName.length());
        }
    }

    public static VolunteerProfile of(
            TelegramId telegramId,
            String fullName,
            PhoneNumber phone,
            District district,
            Car car,
            Availability availability
    ) {
        return new VolunteerProfile(telegramId, fullName, phone, district, car, availability);
    }
}