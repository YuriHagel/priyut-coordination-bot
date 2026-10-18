package ru.priyut.coordination.application.exception;

import ru.priyut.coordination.domain.shared.DomainException;

/**
 * Волонтёр не найден.
 */
public class VolunteerNotFoundException extends DomainException {

    public VolunteerNotFoundException(Long telegramId) {
        super("Volunteer with telegram ID " + telegramId + " not found");
    }
}