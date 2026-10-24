package ru.priyut.coordination.infrastructure.telegram.middleware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.infrastructure.telegram.idempotency.UpdateIdempotencyGuard;

/**
 * Middleware для обеспечения идемпотентности обработки обновлений.
 * Выполняется третьим (порядок 30).
 *
 * Защита от:
 * - Повторной доставки update_id (long polling может доставить дубли)
 * - Дублирования действий (регистрация, согласие на ПДн)
 *
 * Если update_id уже обработан — обновление пропускается.
 */
@Component
public class IdempotencyMiddleware implements Middleware {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyMiddleware.class);

    private final UpdateIdempotencyGuard idempotencyGuard;

    public IdempotencyMiddleware(UpdateIdempotencyGuard idempotencyGuard) {
        this.idempotencyGuard = idempotencyGuard;
    }

    @Override
    public void handle(Update update, UpdateChain chain) {
        var updateId = update.getUpdateId();

        if (!idempotencyGuard.tryAcquire(updateId)) {
            log.warn("🚫 Duplicate update_id={} detected, skipping", updateId);
            return; // Пропускаем дубликат
        }

        // Передаём управление следующему middleware
        chain.proceed(update);
    }

    @Override
    public int order() {
        return 30; // Третий middleware
    }
}