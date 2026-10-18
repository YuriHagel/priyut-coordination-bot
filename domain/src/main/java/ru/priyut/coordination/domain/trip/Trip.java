package ru.priyut.coordination.domain.trip;

import ru.priyut.coordination.domain.shared.DomainException;
import ru.priyut.coordination.domain.shared.UtcClock;
import ru.priyut.coordination.domain.volunteer.VolunteerId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Агрегат "Поездка".
 * Корневая сущность домена, содержит бизнес-правила жизненного цикла поездки.
 */
public final class Trip {

    private final TripId id;
    private final Long createdByAdminTelegramId;
    private final Route route;
    private final LocalDateTime tripDateTime; // в Europe/Moscow
    private final String taskDescription;
    private final TripType type;
    private TripComment comment;
    private TripStatus status;
    private TripAssignment assignment;
    private final Instant createdAt;
    private Instant publishedAt;
    private Instant cancelledAt;
    private String cancelReason;
    private Instant completedAt;
    private Long closedByAdminTelegramId;
    private Instant updatedAt;
    private long version; // для оптимистичной блокировки

    private Trip(
            TripId id,
            Long createdByAdminTelegramId,
            Route route,
            LocalDateTime tripDateTime,
            String taskDescription,
            TripType type,
            TripComment comment,
            TripStatus status,
            TripAssignment assignment,
            Instant createdAt,
            Instant publishedAt,
            Instant cancelledAt,
            String cancelReason,
            Instant completedAt,
            Long closedByAdminTelegramId,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.createdByAdminTelegramId = Objects.requireNonNull(createdByAdminTelegramId);
        this.route = Objects.requireNonNull(route);
        this.tripDateTime = Objects.requireNonNull(tripDateTime);
        this.taskDescription = Objects.requireNonNull(taskDescription);
        this.type = Objects.requireNonNull(type);
        this.comment = Objects.requireNonNull(comment);
        this.status = Objects.requireNonNull(status);
        this.assignment = assignment; // может быть null для DRAFT/PUBLISHED
        this.createdAt = Objects.requireNonNull(createdAt);
        this.publishedAt = publishedAt;
        this.cancelledAt = cancelledAt;
        this.cancelReason = cancelReason;
        this.completedAt = completedAt;
        this.closedByAdminTelegramId = closedByAdminTelegramId;
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    // === Фабричные методы ===

    /**
     * Создание черновика поездки администратором (сценарий 4.2).
     */
    public static Trip createDraft(
            Long adminTelegramId,
            Route route,
            LocalDateTime tripDateTime,
            String taskDescription,
            TripType type,
            TripComment comment,
            Instant now
    ) {
        validateTaskDescription(taskDescription);
        return new Trip(
                TripId.generate(),
                adminTelegramId,
                route,
                tripDateTime,
                taskDescription,
                type,
                comment,
                TripStatus.DRAFT,
                null,
                now, null, null, null, null, null, now, 0L
        );
    }

    /**
     * Реконструкция из БД (для репозитория).
     */
    public static Trip reconstitute(
            TripId id,
            Long createdByAdminTelegramId,
            Route route,
            LocalDateTime tripDateTime,
            String taskDescription,
            TripType type,
            TripComment comment,
            TripStatus status,
            TripAssignment assignment,
            Instant createdAt,
            Instant publishedAt,
            Instant cancelledAt,
            String cancelReason,
            Instant completedAt,
            Long closedByAdminTelegramId,
            Instant updatedAt,
            long version
    ) {
        return new Trip(
                id, createdByAdminTelegramId, route, tripDateTime, taskDescription, type, comment,
                status, assignment, createdAt, publishedAt, cancelledAt, cancelReason,
                completedAt, closedByAdminTelegramId, updatedAt, version
        );
    }

    // === Бизнес-правила ===

    /**
     * Публикация поездки (переход DRAFT → PUBLISHED).
     */
    public void publish(Instant now) {
        if (status != TripStatus.DRAFT) {
            throw new DomainException("Cannot publish trip with status: " + status);
        }
        this.status = TripStatus.PUBLISHED;
        this.publishedAt = now;
        this.updatedAt = now;
    }

    /**
     * Отклик волонтёра на поездку (PUBLISHED → VOLUNTEER_PENDING).
     */
    public void respondByVolunteer(VolunteerId volunteerId, Instant now) {
        if (status != TripStatus.PUBLISHED) {
            throw new DomainException("Cannot respond to trip with status: " + status);
        }
        // Создаём временное назначение (ещё не подтверждено админом)
        this.assignment = TripAssignment.of(volunteerId, null, now);
        this.status = TripStatus.VOLUNTEER_PENDING;
        this.updatedAt = now;
    }

    /**
     * Отклонение отклика волонтёра админом (VOLUNTEER_PENDING → PUBLISHED).
     */
    public void rejectVolunteer(Instant now) {
        if (status != TripStatus.VOLUNTEER_PENDING) {
            throw new DomainException("Cannot reject volunteer with status: " + status);
        }
        this.assignment = null;
        this.status = TripStatus.PUBLISHED;
        this.updatedAt = now;
    }

    /**
     * Подтверждение волонтёра админом (VOLUNTEER_PENDING → ASSIGNED).
     */
    public void confirmVolunteer(Long adminTelegramId, Instant now) {
        if (status != TripStatus.VOLUNTEER_PENDING) {
            throw new DomainException("Cannot confirm volunteer with status: " + status);
        }
        if (assignment == null) {
            throw new DomainException("No volunteer to confirm");
        }
        this.assignment = TripAssignment.of(assignment.volunteerId(), adminTelegramId, now);
        this.status = TripStatus.ASSIGNED;
        this.updatedAt = now;
    }

    /**
     * Отмена поездки волонтёром (сценарий 4.4).
     * Возвращает поездку в статус PUBLISHED для поиска нового волонтёра.
     */
    public void cancelByVolunteer(String reason, Instant now) {
        if (status != TripStatus.ASSIGNED) {
            throw new DomainException("Volunteer can only cancel ASSIGNED trip, got: " + status);
        }
        this.assignment = null;
        this.status = TripStatus.PUBLISHED;
        this.cancelReason = reason;
        this.updatedAt = now;
    }

    /**
     * Отмена поездки админом (сценарий 4.4).
     */
    public void cancelByAdmin(Long adminTelegramId, String reason, Instant now) {
        if (!status.canBeCancelled()) {
            throw new DomainException("Cannot cancel trip with status: " + status);
        }
        this.status = TripStatus.CANCELLED;
        this.cancelledAt = now;
        this.cancelReason = reason;
        this.updatedAt = now;
    }

    /**
     * Изменение времени поездки (сценарий 4.4).
     * Если поездка была ASSIGNED — переходит в RESCHEDULED_PENDING.
     */
    public void reschedule(LocalDateTime newDateTime, Instant now) {
        if (!status.canBeCancelled()) {
            throw new DomainException("Cannot reschedule trip with status: " + status);
        }
        this.tripDateTime = newDateTime;
        if (status == TripStatus.ASSIGNED) {
            this.status = TripStatus.RESCHEDULED_PENDING;
        }
        this.updatedAt = now;
    }

    /**
     * Волонтёр подтверждает новое время после переноса (RESCHEDULED_PENDING → ASSIGNED).
     */
    public void confirmReschedule(Instant now) {
        if (status != TripStatus.RESCHEDULED_PENDING) {
            throw new DomainException("Cannot confirm reschedule with status: " + status);
        }
        this.status = TripStatus.ASSIGNED;
        this.updatedAt = now;
    }

    /**
     * Волонтёр отказывается от нового времени (RESCHEDULED_PENDING → PUBLISHED).
     */
    public void rejectReschedule(Instant now) {
        if (status != TripStatus.RESCHEDULED_PENDING) {
            throw new DomainException("Cannot reject reschedule with status: " + status);
        }
        this.assignment = null;
        this.status = TripStatus.PUBLISHED;
        this.updatedAt = now;
    }

    /**
     * Волонтёр отмечает поездку выполненной (ASSIGNED → PENDING_COMPLETION).
     * Двухэтапное завершение (сценарий 4.6, шаг 1).
     */
    public void markCompletedByVolunteer(Instant now) {
        if (status != TripStatus.ASSIGNED) {
            throw new DomainException("Volunteer can only complete ASSIGNED trip, got: " + status);
        }
        this.status = TripStatus.PENDING_COMPLETION;
        this.updatedAt = now;
    }

    /**
     * Админ возвращает поездку на доработку (PENDING_COMPLETION → ASSIGNED).
     */
    public void returnForRework(Instant now) {
        if (status != TripStatus.PENDING_COMPLETION) {
            throw new DomainException("Cannot return trip with status: " + status);
        }
        this.status = TripStatus.ASSIGNED;
        this.updatedAt = now;
    }

    /**
     * Админ закрывает поездку (PENDING_COMPLETION → COMPLETED).
     * Двухэтапное завершение (сценарий 4.6, шаг 2).
     */
    public void closeByAdmin(Long adminTelegramId, Instant now) {
        if (status != TripStatus.PENDING_COMPLETION) {
            throw new DomainException("Admin can only close PENDING_COMPLETION trip, got: " + status);
        }
        this.status = TripStatus.COMPLETED;
        this.completedAt = now;
        this.closedByAdminTelegramId = adminTelegramId;
        this.updatedAt = now;
    }

    /**
     * Обновление комментария (только для активных поездок).
     */
    public void updateComment(TripComment comment, Instant now) {
        if (!status.isActive()) {
            throw new DomainException("Cannot update comment for inactive trip");
        }
        this.comment = comment;
        this.updatedAt = now;
    }

    /**
     * Увеличение версии (для оптимистичной блокировки).
     */
    public void incrementVersion() {
        this.version++;
    }

    // === Геттеры ===

    public TripId getId() { return id; }
    public Long getCreatedByAdminTelegramId() { return createdByAdminTelegramId; }
    public Route getRoute() { return route; }
    public LocalDateTime getTripDateTime() { return tripDateTime; }
    public String getTaskDescription() { return taskDescription; }
    public TripType getType() { return type; }
    public TripComment getComment() { return comment; }
    public TripStatus getStatus() { return status; }
    public TripAssignment getAssignment() { return assignment; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public String getCancelReason() { return cancelReason; }
    public Instant getCompletedAt() { return completedAt; }
    public Long getClosedByAdminTelegramId() { return closedByAdminTelegramId; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }

    /**
     * ID назначенного волонтёра (если есть).
     */
    public VolunteerId getAssignedVolunteerId() {
        return assignment != null ? assignment.volunteerId() : null;
    }

    public boolean isUrgent() {
        return type.isUrgent();
    }

    // === Вспомогательные методы ===

    private static void validateTaskDescription(String taskDescription) {
        Objects.requireNonNull(taskDescription, "Task description must not be null");
        if (taskDescription.trim().isEmpty()) {
            throw new DomainException("Task description must not be empty");
        }
        if (taskDescription.length() > 500) {
            throw new DomainException("Task description is too long");
        }
    }
}