package ru.priyut.coordination.domain.trip;

/**
 * Статусная модель поездки.
 */
public enum TripStatus {
    /** Черновик, создан админом, но не опубликован */
    DRAFT,

    /** Опубликована и доступна для откликов */
    PUBLISHED,

    /** Есть отклик волонтёра, ждёт подтверждения админом */
    VOLUNTEER_PENDING,

    /** Админ подтвердил волонтёра, поездка занята */
    ASSIGNED,

    /** Волонтёр отметил выполнение, ждёт закрытия админом */
    PENDING_COMPLETION,

    /** Поездка полностью закрыта и сохранена в историю */
    COMPLETED,

    /** Поездка отменена */
    CANCELLED,

    /** Время изменено, ждёт подтверждения волонтёром */
    RESCHEDULED_PENDING;

    /**
     * Может ли поездка в этом статусе принимать отклики?
     */
    public boolean canAcceptResponses() {
        return this == PUBLISHED;
    }

    /**
     * Можно ли отменить поездку в этом статусе?
     */
    public boolean canBeCancelled() {
        return this != COMPLETED && this != CANCELLED;
    }

    /**
     * Является ли поездка активной (требует внимания)?
     */
    public boolean isActive() {
        return this == PUBLISHED
                || this == VOLUNTEER_PENDING
                || this == ASSIGNED
                || this == PENDING_COMPLETION
                || this == RESCHEDULED_PENDING;
    }
}