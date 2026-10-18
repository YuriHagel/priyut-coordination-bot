package ru.priyut.coordination.domain.trip;

/**
 * Тип поездки.
 */
public enum TripType {
    /** Обычная плановая поездка */
    REGULAR,

    /** Срочная поездка — привлекает больше внимания волонтёров */
    URGENT;

    public boolean isUrgent() {
        return this == URGENT;
    }
}