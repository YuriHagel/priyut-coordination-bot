package ru.priyut.coordination.domain.consent;

import ru.priyut.coordination.domain.shared.DomainException;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.time.Instant;
import java.util.Objects;

/**
 * Агрегат "Согласие на обработку ПДн".
 * Управляет жизненным циклом согласия одного пользователя.
 */
public final class Consent {

    private final TelegramId telegramId;
    private ConsentVersion currentVersion;
    private Instant grantedAt;
    private Instant withdrawnAt;
    private String withdrawalReason;
    private boolean active;

    private Consent(
            TelegramId telegramId,
            ConsentVersion currentVersion,
            Instant grantedAt,
            Instant withdrawnAt,
            String withdrawalReason,
            boolean active
    ) {
        this.telegramId = Objects.requireNonNull(telegramId);
        this.currentVersion = Objects.requireNonNull(currentVersion);
        this.grantedAt = Objects.requireNonNull(grantedAt);
        this.withdrawnAt = withdrawnAt;
        this.withdrawalReason = withdrawalReason;
        this.active = active;
    }

    /**
     * Создание нового согласия при регистрации (сценарий 4.1).
     */
    public static Consent grant(
            TelegramId telegramId,
            ConsentVersion version,
            Instant grantedAt
    ) {
        return new Consent(telegramId, version, grantedAt, null, null, true);
    }

    /**
     * Реконструкция из БД.
     */
    public static Consent reconstitute(
            TelegramId telegramId,
            ConsentVersion currentVersion,
            Instant grantedAt,
            Instant withdrawnAt,
            String withdrawalReason,
            boolean active
    ) {
        return new Consent(telegramId, currentVersion, grantedAt, withdrawnAt, withdrawalReason, active);
    }

    // === Бизнес-правила ===

    /**
     * Обновление согласия при изменении версии политики.
     */
    public void updateVersion(ConsentVersion newVersion, Instant now) {
        if (!active) {
            throw new DomainException("Cannot update version for withdrawn consent");
        }
        this.currentVersion = newVersion;
        this.grantedAt = now;
    }

    /**
     * Отзыв согласия (сценарий 4.8).
     */
    public void withdraw(String reason, Instant now) {
        if (!active) {
            throw new DomainException("Consent is already withdrawn");
        }
        this.active = false;
        this.withdrawnAt = now;
        this.withdrawalReason = reason == null ? "" : reason.trim();
    }

    /**
     * Повторная выдача согласия после отзыва.
     */
    public void regrant(ConsentVersion version, Instant now) {
        this.currentVersion = version;
        this.grantedAt = now;
        this.withdrawnAt = null;
        this.withdrawalReason = null;
        this.active = true;
    }

    // === Геттеры ===

    public TelegramId getTelegramId() { return telegramId; }
    public ConsentVersion getCurrentVersion() { return currentVersion; }
    public Instant getGrantedAt() { return grantedAt; }
    public Instant getWithdrawnAt() { return withdrawnAt; }
    public String getWithdrawalReason() { return withdrawalReason; }
    public boolean isActive() { return active; }
}