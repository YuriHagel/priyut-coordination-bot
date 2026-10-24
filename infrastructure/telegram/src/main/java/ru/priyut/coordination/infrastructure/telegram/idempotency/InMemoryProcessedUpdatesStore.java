package ru.priyut.coordination.infrastructure.telegram.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.priyut.coordination.domain.shared.UtcClock;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryProcessedUpdatesStore implements ProcessedUpdatesStore {

    private static final Logger log = LoggerFactory.getLogger(InMemoryProcessedUpdatesStore.class);

    private final ConcurrentHashMap<Long, Instant> processedUpdates = new ConcurrentHashMap<>();
    private final UtcClock clock;

    public InMemoryProcessedUpdatesStore(UtcClock clock) {
        this.clock = clock;
    }

    @Override
    public boolean tryMarkAsProcessed(long updateId) {
        Instant now = clock.now();
        Instant previous = processedUpdates.putIfAbsent(updateId, now);
        return previous == null;
    }

    /**
     * 1. Реализация метода интерфейса.
     * Содержит всю логику очистки и может быть вызвана вручную (например, из тестов).
     */
    @Override
    public void cleanupExpired(Duration ttl) {
        Instant now = clock.now();
        int initialSize = processedUpdates.size();

        boolean removed = processedUpdates.entrySet().removeIf(entry ->
                Duration.between(entry.getValue(), now).compareTo(ttl) > 0
        );

        if (removed) {
            // Вычисляем точное количество удаленных записей
            int removedCount = initialSize - processedUpdates.size();
            log.debug("🧹 Cleaned up {} expired update IDs. Current size: {}", removedCount, processedUpdates.size());
        }
    }

    /**
     * 2. Метод для Spring Scheduler.
     * Не имеет аргументов, поэтому корректно работает с @Scheduled.
     * Вызывает основной метод очистки с дефолтным TTL для продакшена (24 часа).
     */
    @Scheduled(fixedRate = 3_600_000) // Запускать каждый час
    public void scheduledCleanup() {
        cleanupExpired(Duration.ofHours(24));
    }

    @Override
    public int size() {
        return processedUpdates.size();
    }
}