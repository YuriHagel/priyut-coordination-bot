package ru.priyut.coordination.domain.volunteer;

/**
 * Статус волонтёра в системе.
 */
public enum VolunteerStatus {
    /** Анкета заполнена, ожидает проверки админом */
    PENDING_MODERATION,

    /** Анкета одобрена, волонтёр может брать поездки */
    ACTIVE,

    /** Анкета отклонена администратором */
    REJECTED,

    /** Волонтёр временно недоступен (не получает новые назначения) */
    TEMPORARILY_UNAVAILABLE,

    /** Запрошено удаление персональных данных */
    DELETION_REQUESTED,

    /** Персональные данные удалены */
    DELETED,

    /** Профиль архивирован после 3 лет неактивности */
    ARCHIVED;

    /**
     * Может ли волонтёр с таким статусом откликаться на поездки?
     */
    public boolean canRespondToTrips() {
        return this == ACTIVE;
    }

    /**
     * Можно ли редактировать профиль с таким статусом?
     */
    public boolean canEditProfile() {
        return this == ACTIVE || this == TEMPORARILY_UNAVAILABLE;
    }
}