package ru.priyut.coordination.infrastructure.telegram.middleware;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Интерфейс middleware для обработки Telegram updates.
 * Реализует паттерн Chain of Responsibility.
 *
 * Middleware могут:
 * - Логировать обновления
 * - Собирать метрики
 * - Проверять авторизацию
 * - Обеспечивать идемпотентность
 * - Модифицировать update (редко)
 * - Прерывать цепочку (если условие не выполнено)
 */
public interface Middleware {

    /**
     * Обработка обновления.
     *
     * @param update входящее обновление от Telegram
     * @param chain  следующая цепочка обработки
     */
    void handle(Update update, UpdateChain chain);

    /**
     * Приоритет middleware (чем меньше число, тем раньше выполняется).
     * По умолчанию: 100.
     *
     * Рекомендуемые приоритеты:
     * - Logging: 10 (первым логируем)
     * - Metrics: 20 (считаем время всей обработки)
     * - Idempotency: 30 (проверяем дубли до авторизации)
     * - AdminAuth: 40 (проверяем права перед бизнес-логикой)
     */
    default int order() {
        return 100;
    }
}