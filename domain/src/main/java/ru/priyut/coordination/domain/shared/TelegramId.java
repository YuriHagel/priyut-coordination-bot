package ru.priyut.coordination.domain.shared;

import java.util.Objects;

/**
 * Уникальный идентификатор пользователя в Telegram.
 * Value object: неизменяемый, с валидацией.
 */
public record TelegramId(long value) {

    public TelegramId {
        if (value <= 0) {
            throw new DomainException("Telegram ID must be positive, got: " + value);
        }
    }

    public static TelegramId of(long value) {
        return new TelegramId(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}