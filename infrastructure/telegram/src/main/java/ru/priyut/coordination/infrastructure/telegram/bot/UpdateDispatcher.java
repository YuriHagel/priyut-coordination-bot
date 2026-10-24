package ru.priyut.coordination.infrastructure.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.infrastructure.telegram.middleware.Middleware;
import ru.priyut.coordination.infrastructure.telegram.middleware.UpdateChain;

import java.util.Comparator;
import java.util.List;

/**
 * Центральный диспетчер Telegram updates.
 * Применяет цепочку middleware и передаёт управление финальному обработчику.
 *
 * Порядок обработки:
 * 1. LoggingMiddleware (порядок 10)
 * 2. MetricsMiddleware (порядок 20)
 * 3. IdempotencyMiddleware (порядок 30)
 * 4. AdminAuthorizationMiddleware (порядок 40)
 * 5. Финальный обработчик (CommandHandler, CallbackHandler, Dialog)
 */
@Component
public class UpdateDispatcher {

    private static final Logger log = LoggerFactory.getLogger(UpdateDispatcher.class);

    private final List<Middleware> middlewares;
    private final FinalUpdateHandler finalHandler;

    public UpdateDispatcher(
            List<Middleware> middlewares,
            FinalUpdateHandler finalHandler
    ) {
        // Сортируем middleware по приоритету
        this.middlewares = middlewares.stream()
                .sorted(Comparator.comparingInt(Middleware::order))
                .toList();
        this.finalHandler = finalHandler;

        log.info("✅ UpdateDispatcher initialized with {} middlewares: {}",
                middlewares.size(),
                middlewares.stream()
                        .map(m -> m.getClass().getSimpleName() + "(order=" + m.order() + ")")
                        .toList());
    }

    /**
     * Обработка входящего обновления от Telegram.
     */
    public void dispatch(Update update) {
        if (update == null) {
            log.warn("Received null update, skipping");
            return;
        }

        log.debug("Dispatching update_id={}", update.getUpdateId());

        // Запускаем цепочку middleware
        var chain = new DefaultUpdateChain(middlewares, finalHandler);
        chain.proceed(update);
    }

    /**
     * Реализация цепочки обработки.
     */
    private static class DefaultUpdateChain implements UpdateChain {

        private final List<Middleware> middlewares;
        private final FinalUpdateHandler finalHandler;
        private int currentIndex = 0;

        DefaultUpdateChain(List<Middleware> middlewares, FinalUpdateHandler finalHandler) {
            this.middlewares = middlewares;
            this.finalHandler = finalHandler;
        }

        @Override
        public void proceed(Update update) {
            if (currentIndex < middlewares.size()) {
                // Вызываем следующий middleware
                var middleware = middlewares.get(currentIndex++);
                middleware.handle(update, this);
            } else {
                // Все middleware пройдены — вызываем финальный обработчик
                finalHandler.handle(update);
            }
        }
    }
}