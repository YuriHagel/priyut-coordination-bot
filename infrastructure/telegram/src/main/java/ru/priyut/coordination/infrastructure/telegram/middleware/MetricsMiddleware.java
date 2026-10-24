package ru.priyut.coordination.infrastructure.telegram.middleware;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Middleware для сбора метрик обработки обновлений.
 * Выполняется вторым (порядок 20).
 *
 * Метрики:
 * - priyut_telegram_updates_total{type=command|callback|text} — количество обновлений
 * - priyut_telegram_update_duration_seconds — время обработки
 */
@Component
public class MetricsMiddleware implements Middleware {

    private final MeterRegistry meterRegistry;
    private final Counter updatesTotal;
    private final Timer updateDuration;

    public MetricsMiddleware(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.updatesTotal = Counter.builder("priyut_telegram_updates_total")
                .description("Total number of Telegram updates processed")
                .tag("status", "success")
                .register(meterRegistry);

        this.updateDuration = Timer.builder("priyut_telegram_update_duration_seconds")
                .description("Time spent processing Telegram updates")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    @Override
    public void handle(Update update, UpdateChain chain) {
        var type = getUpdateType(update);
        var sample = Timer.start(meterRegistry);

        try {
            chain.proceed(update);
            updatesTotal.increment();
        } catch (Exception e) {
            meterRegistry.counter("priyut_telegram_updates_total", "status", "error")
                    .increment();
            throw e;
        } finally {
            sample.stop(updateDuration);
        }
    }

    @Override
    public int order() {
        return 20; // Второй middleware
    }

    private String getUpdateType(Update update) {
        if (update.hasMessage() && update.getMessage().isCommand()) {
            return "command";
        }
        if (update.hasCallbackQuery()) {
            return "callback";
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            return "text";
        }
        return "other";
    }
}