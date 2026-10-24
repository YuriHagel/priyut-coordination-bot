package ru.priyut.coordination.infrastructure.telegram.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Сборщик кастомных метрик для Telegram-инфраструктуры.
 * Дополняет стандартные метрики Resilience4j и Spring Boot Actuator.
 */
@Component
public class TelegramMetricsCollector {

    private final MeterRegistry meterRegistry;

    // Счетчики
    private Counter messagesSentCounter;
    private Counter messagesFailedCounter;
    private Counter broadcastsInitiatedCounter;

    // Таймеры
    private Timer messageSendTimer;

    public TelegramMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        messagesSentCounter = Counter.builder("priyut_telegram_messages_sent_total")
                .description("Total number of successfully sent Telegram messages")
                .register(meterRegistry);

        messagesFailedCounter = Counter.builder("priyut_telegram_messages_failed_total")
                .description("Total number of failed Telegram message attempts")
                .register(meterRegistry);

        broadcastsInitiatedCounter = Counter.builder("priyut_telegram_broadcasts_initiated_total")
                .description("Total number of batch broadcast operations started")
                .register(meterRegistry);

        messageSendTimer = Timer.builder("priyut_telegram_message_send_duration_seconds")
                .description("Time taken to send a single Telegram message")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public void recordMessageSent() {
        messagesSentCounter.increment();
    }

    public void recordMessageFailed() {
        messagesFailedCounter.increment();
    }

    public void recordBroadcastInitiated(int recipientCount) {
        broadcastsInitiatedCounter.increment();
        // Можно также добавить Gauge или DistributionSummary для размера батча
        meterRegistry.summary("priyut_telegram_broadcast_batch_size")
                .record(recipientCount);
    }

    public <T> T recordMessageSendTime(java.util.concurrent.Callable<T> action) throws Exception {
        return messageSendTimer.recordCallable(action);
    }
}