package ru.priyut.coordination.infrastructure.telegram.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Утилита для замера времени обработки бизнес-логики внутри Telegram-хендлеров.
 * Полезно для выделения "медленных" команд или диалогов.
 */
@Component
public class UpdateProcessingTimer {

    private final Timer handlerExecutionTimer;

    public UpdateProcessingTimer(MeterRegistry meterRegistry) {
        this.handlerExecutionTimer = Timer.builder("priyut_telegram_handler_execution_seconds")
                .description("Time spent executing business logic for a Telegram update")
                .tag("layer", "handler")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    /**
     * Измеряет время выполнения переданного действия.
     */
    public <T> T time(String handlerName, java.util.concurrent.Callable<T> action) throws Exception {
        return handlerExecutionTimer.recordCallable(action);
    }

    /**
     * Измеряет время выполнения действия без возвращаемого значения.
     */
    public void time(String handlerName, Runnable action) {
        handlerExecutionTimer.record(action);
    }
}