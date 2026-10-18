package ru.priyut.coordination.application.command;

import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.volunteer.Availability;
import ru.priyut.coordination.domain.volunteer.Car;
import ru.priyut.coordination.domain.volunteer.District;
import ru.priyut.coordination.domain.volunteer.PhoneNumber;

/**
 * Команда для регистрации волонтёра.
 */
public record RegisterVolunteerCommand(
        TelegramId telegramId,
        String fullName,
        PhoneNumber phone,
        District district,
        Car car,
        Availability availability,
        String consentVersion
) {
}