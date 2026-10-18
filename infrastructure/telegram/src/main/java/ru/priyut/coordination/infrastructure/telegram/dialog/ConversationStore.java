package ru.priyut.coordination.infrastructure.telegram.dialog;

import ru.priyut.coordination.domain.shared.TelegramId;

import java.util.Optional;

/**
 * Порт для хранения состояний активных диалогов.
 * Реализация может быть in-memory (MVP) или Redis/PostgreSQL (production).
 */
public interface ConversationStore {

    /**
     * Загрузить активный диалог пользователя.
     */
    Optional<ConversationContext> load(TelegramId telegramId);

    /**
     * Сохранить состояние диалога.
     */
    void save(ConversationContext context);

    /**
     * Удалить диалог (после завершения или отмены).
     */
    void delete(TelegramId telegramId);

    /**
     * Очистить все просроченные диалоги (для фоновой задачи).
     */
    int cleanupExpired(java.time.Duration ttl);
}