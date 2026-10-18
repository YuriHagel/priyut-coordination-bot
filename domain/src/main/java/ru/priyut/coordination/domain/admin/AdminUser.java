package ru.priyut.coordination.domain.admin;

import ru.priyut.coordination.domain.shared.DomainException;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.util.Objects;

/**
 * Пользователь-администратор.
 * Value object: неизменяемый.
 */
public record AdminUser(TelegramId telegramId, String displayName) {

    public AdminUser {
        Objects.requireNonNull(telegramId, "telegramId must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");

        displayName = displayName.trim();
        if (displayName.isEmpty()) {
            throw new DomainException("Admin display name must not be empty");
        }
        if (displayName.length() > 100) {
            throw new DomainException("Admin display name is too long");
        }
    }

    public static AdminUser of(TelegramId telegramId, String displayName) {
        return new AdminUser(telegramId, displayName);
    }
}