package ru.priyut.coordination.domain.volunteer;

import ru.priyut.coordination.domain.shared.DomainException;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.shared.UtcClock;

import java.time.Instant;
import java.util.Objects;

/**
 * Агрегат "Волонтёр".
 * Корневая сущность домена, содержит бизнес-правила.
 */
public final class Volunteer {

    private final VolunteerId id;
    private final TelegramId telegramId;
    private VolunteerStatus status;
    private String fullName;
    private PhoneNumber phone;
    private District district;
    private Car car;
    private Availability availability;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastActivityAt;

    private Volunteer(
            VolunteerId id,
            TelegramId telegramId,
            VolunteerStatus status,
            String fullName,
            PhoneNumber phone,
            District district,
            Car car,
            Availability availability,
            Instant createdAt,
            Instant updatedAt,
            Instant lastActivityAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.telegramId = Objects.requireNonNull(telegramId);
        this.status = Objects.requireNonNull(status);
        this.fullName = Objects.requireNonNull(fullName);
        this.phone = Objects.requireNonNull(phone);
        this.district = Objects.requireNonNull(district);
        this.car = Objects.requireNonNull(car);
        this.availability = Objects.requireNonNull(availability);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.lastActivityAt = Objects.requireNonNull(lastActivityAt);
    }

    /**
     * Фабричный метод: создание нового волонтёра после регистрации.
     * Статус автоматически устанавливается в PENDING_MODERATION.
     */
    public static Volunteer register(VolunteerProfile profile, Instant now) {
        return new Volunteer(
                VolunteerId.generate(),
                profile.telegramId(),
                VolunteerStatus.PENDING_MODERATION,
                profile.fullName(),
                profile.phone(),
                profile.district(),
                profile.car(),
                profile.availability(),
                now,
                now,
                now
        );
    }

    /**
     * Реконструкция из БД (для репозитория).
     */
    public static Volunteer reconstitute(
            VolunteerId id,
            TelegramId telegramId,
            VolunteerStatus status,
            String fullName,
            PhoneNumber phone,
            District district,
            Car car,
            Availability availability,
            Instant createdAt,
            Instant updatedAt,
            Instant lastActivityAt
    ) {
        return new Volunteer(
                id, telegramId, status, fullName, phone, district, car, availability,
                createdAt, updatedAt, lastActivityAt
        );
    }

    // === Бизнес-правила ===

    /**
     * Одобрение анкеты администратором.
     */
    public void approve(Instant now) {
        if (status != VolunteerStatus.PENDING_MODERATION) {
            throw new DomainException(
                    "Cannot approve volunteer with status: " + status);
        }
        this.status = VolunteerStatus.ACTIVE;
        this.updatedAt = now;
    }

    /**
     * Отклонение анкеты администратором.
     */
    public void reject(Instant now) {
        if (status != VolunteerStatus.PENDING_MODERATION) {
            throw new DomainException(
                    "Cannot reject volunteer with status: " + status);
        }
        this.status = VolunteerStatus.REJECTED;
        this.updatedAt = now;
    }

    /**
     * Переключение статуса доступности.
     */
    public void toggleAvailability(boolean available, Instant now) {
        ensureEditable();
        this.status = available
                ? VolunteerStatus.ACTIVE
                : VolunteerStatus.TEMPORARILY_UNAVAILABLE;
        this.updatedAt = now;
    }

    /**
     * Обновление профиля волонтёра.
     */
    public void updateProfile(VolunteerProfile profile, Instant now) {
        ensureEditable();
        this.fullName = profile.fullName();
        this.phone = profile.phone();
        this.district = profile.district();
        this.car = profile.car();
        this.availability = profile.availability();
        this.updatedAt = now;
    }

    /**
     * Запрос на удаление профиля (152-ФЗ).
     */
    public void requestDeletion(Instant now) {
        if (status == VolunteerStatus.DELETED) {
            throw new DomainException("Volunteer is already deleted");
        }
        this.status = VolunteerStatus.DELETION_REQUESTED;
        this.updatedAt = now;
    }

    /**
     * Фактическое удаление персональных данных (после обработки запроса).
     */
    public void deletePersonalData(Instant now) {
        this.fullName = "Удалённый волонтёр";
        this.phone = PhoneNumber.of("+70000000000");
        this.district = District.of("—");
        this.car = Car.of("—", "—", "");
        this.status = VolunteerStatus.DELETED;
        this.updatedAt = now;
    }

    /**
     * Архивация после 3 лет неактивности.
     */
    public void archive(Instant now) {
        this.status = VolunteerStatus.ARCHIVED;
        this.updatedAt = now;
    }

    /**
     * Фиксация активности (например, при отклике на поездку).
     */
    public void markActivity(Instant now) {
        this.lastActivityAt = now;
    }

    /**
     * Может ли волонтёр откликаться на поездки?
     */
    public boolean canRespondToTrips() {
        return status.canRespondToTrips();
    }

    // === Геттеры (без сеттеров — состояние меняется только через бизнес-методы) ===

    public VolunteerId getId() { return id; }
    public TelegramId getTelegramId() { return telegramId; }
    public VolunteerStatus getStatus() { return status; }
    public String getFullName() { return fullName; }
    public PhoneNumber getPhone() { return phone; }
    public District getDistrict() { return district; }
    public Car getCar() { return car; }
    public Availability getAvailability() { return availability; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getLastActivityAt() { return lastActivityAt; }

    // === Вспомогательные методы ===

    private void ensureEditable() {
        if (!status.canEditProfile()) {
            throw new DomainException(
                    "Cannot edit volunteer with status: " + status);
        }
    }
}