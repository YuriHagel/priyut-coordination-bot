package ru.priyut.coordination.infrastructure.telegram.dialog;

import ru.priyut.coordination.domain.shared.TelegramId;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Состояние активного диалога пользователя.
 * Хранит:
 * - кто ведёт диалог
 * - на каком шаге находится
 * - какие данные уже собраны
 * - метаданные (время создания, TTL)
 */
public final class ConversationContext {

    /**
     * Тип диалога (для маршрутизации).
     */
    public enum DialogType {
        VOLUNTEER_REGISTRATION,
        TRIP_CREATION,
        TRIP_CANCEL
    }

    private final TelegramId telegramId;
    private final DialogType dialogType;
    private final Instant startedAt;
    private Instant lastActivityAt;
    private int currentStepIndex;
    private final Map<String, Object> data;

    private ConversationContext(
            TelegramId telegramId,
            DialogType dialogType,
            Instant startedAt,
            Instant lastActivityAt,
            int currentStepIndex,
            Map<String, Object> data
    ) {
        this.telegramId = Objects.requireNonNull(telegramId);
        this.dialogType = Objects.requireNonNull(dialogType);
        this.startedAt = Objects.requireNonNull(startedAt);
        this.lastActivityAt = Objects.requireNonNull(lastActivityAt);
        this.currentStepIndex = currentStepIndex;
        this.data = new HashMap<>(data);
    }

    /**
     * Создание нового контекста для диалога.
     */
    public static ConversationContext start(TelegramId telegramId, DialogType type, Instant now) {
        return new ConversationContext(telegramId, type, now, now, 0, new HashMap<>());
    }

    // === Методы для работы с данными ===

    public void put(String key, Object value) {
        data.put(key, Objects.requireNonNull(value));
        this.lastActivityAt = Instant.now();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) return Optional.empty();
        if (!type.isInstance(value)) {
            throw new IllegalStateException(
                    "Expected " + type.getSimpleName() + " but got " + value.getClass().getSimpleName());
        }
        return Optional.of((T) value);
    }

    public <T> T require(String key, Class<T> type) {
        return get(key, type).orElseThrow(() ->
                new IllegalStateException("Required data key missing: " + key));
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    // === Навигация по шагам ===

    public void advanceToNextStep() {
        currentStepIndex++;
        lastActivityAt = Instant.now();
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    // === Геттеры ===

    public TelegramId getTelegramId() { return telegramId; }
    public DialogType getDialogType() { return dialogType; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getLastActivityAt() { return lastActivityAt; }
    public Map<String, Object> getData() { return Map.copyOf(data); }

    /**
     * Проверка, не истёк ли контекст (по умолчанию 30 минут неактивности).
     */
    public boolean isExpired(Instant now, java.time.Duration ttl) {
        return java.time.Duration.between(lastActivityAt, now).compareTo(ttl) > 0;
    }
}