package ru.priyut.coordination.domain.volunteer;

import ru.priyut.coordination.domain.shared.DomainException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Номер телефона волонтёра.
 * Валидирует формат +7XXXXXXXXXX (11 цифр после +).
 */
public record PhoneNumber(String value) {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\d{10}$");

    public PhoneNumber {
        Objects.requireNonNull(value, "Phone number must not be null");
        String normalized = value.replaceAll("[\\s\\-()]", "");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new DomainException(
                    "Invalid phone number format. Expected: +7XXXXXXXXXX, got: " + value);
        }
        value = normalized; // сохраняем нормализованную версию
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    /**
     * Маскирование для логов: +7 999 ***-45-67
     */
    public String masked() {
        if (value.length() < 7) return "***";
        return value.substring(0, 6) + "***" + value.substring(value.length() - 4);
    }

    @Override
    public String toString() {
        return value;
    }
}