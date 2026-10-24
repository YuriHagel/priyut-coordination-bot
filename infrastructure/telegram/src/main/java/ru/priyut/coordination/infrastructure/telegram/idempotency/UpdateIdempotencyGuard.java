package ru.priyut.coordination.infrastructure.telegram.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Страж идемпотентности.
 * Проверяет, не обрабатывалось ли данное обновление ранее.
 */
@Component
public class UpdateIdempotencyGuard {

    private static final Logger log = LoggerFactory.getLogger(UpdateIdempotencyGuard.class);

    private final ProcessedUpdatesStore store;

    public UpdateIdempotencyGuard(ProcessedUpdatesStore store) {
        this.store = store;
    }

    /**
     * Пытается получить разрешение на обработку обновления.
     *
     * @param updateId Идентификатор обновления из Telegram
     * @return true, если обновление новое и его можно обрабатывать.
     *         false, если обновление уже обрабатывалось (его нужно игнорировать).
     */
    public boolean tryAcquire(long updateId) {
        boolean isNew = store.tryMarkAsProcessed(updateId);

        if (!isNew) {
            log.warn("🚫 Duplicate update_id={} detected. Skipping to prevent double-processing.", updateId);
        }

        return isNew;
    }
}