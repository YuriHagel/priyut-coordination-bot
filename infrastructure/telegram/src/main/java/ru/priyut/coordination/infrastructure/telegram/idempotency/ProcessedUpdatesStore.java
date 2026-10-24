package ru.priyut.coordination.infrastructure.telegram.idempotency;

import java.time.Duration;

public interface ProcessedUpdatesStore {

    boolean tryMarkAsProcessed(long updateId);

    /**
     * Очищает устаревшие записи.
     * @param ttl Максимальное время хранения записи
     */
    void cleanupExpired(Duration ttl);

    int size();
}