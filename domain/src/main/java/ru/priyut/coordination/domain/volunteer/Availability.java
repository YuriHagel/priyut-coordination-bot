package ru.priyut.coordination.domain.volunteer;

import ru.priyut.coordination.domain.shared.DomainException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Режим доступности волонтёра.
 * Включает дни недели, временной интервал и общий статус доступности.
 */
public record Availability(
        Set<DayOfWeek> daysOfWeek,
        LocalTime timeFrom,
        LocalTime timeTo,
        AvailabilityStatus status
) {

    public enum AvailabilityStatus {
        AVAILABLE,
        TEMPORARILY_UNAVAILABLE
    }

    public Availability {
        Objects.requireNonNull(daysOfWeek, "daysOfWeek must not be null");
        Objects.requireNonNull(timeFrom, "timeFrom must not be null");
        Objects.requireNonNull(timeTo, "timeTo must not be null");
        Objects.requireNonNull(status, "status must not be null");

        if (daysOfWeek.isEmpty()) {
            throw new DomainException("At least one day of week must be selected");
        }
        if (!timeFrom.isBefore(timeTo)) {
            throw new DomainException(
                    "timeFrom must be before timeTo, got: " + timeFrom + " - " + timeTo);
        }

        // Защищённая копия
        daysOfWeek = EnumSet.copyOf(daysOfWeek);
    }

    public static Availability of(
            Set<DayOfWeek> daysOfWeek,
            LocalTime timeFrom,
            LocalTime timeTo,
            AvailabilityStatus status
    ) {
        return new Availability(daysOfWeek, timeFrom, timeTo, status);
    }

    /**
     * Доступен ли волонтёр в указанный день и время?
     */
    public boolean isAvailableAt(DayOfWeek day, LocalTime time) {
        return status == AvailabilityStatus.AVAILABLE
                && daysOfWeek.contains(day)
                && !time.isBefore(timeFrom)
                && !time.isAfter(timeTo);
    }

    /**
     * Форматированное представление: "Пн–Пт, 13:00–18:00"
     */
    public String formatted() {
        StringBuilder sb = new StringBuilder();
        var sortedDays = daysOfWeek.stream().sorted().toList();
        for (int i = 0; i < sortedDays.size(); i++) {
            sb.append(shortName(sortedDays.get(i)));
            if (i < sortedDays.size() - 1) sb.append(", ");
        }
        sb.append(", ").append(timeFrom).append("–").append(timeTo);
        return sb.toString();
    }

    private static String shortName(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Пн";
            case TUESDAY -> "Вт";
            case WEDNESDAY -> "Ср";
            case THURSDAY -> "Чт";
            case FRIDAY -> "Пт";
            case SATURDAY -> "Сб";
            case SUNDAY -> "Вс";
        };
    }

    @Override
    public String toString() {
        return formatted();
    }
}