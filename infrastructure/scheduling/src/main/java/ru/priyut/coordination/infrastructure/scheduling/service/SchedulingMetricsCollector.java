package ru.priyut.coordination.infrastructure.scheduling.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Сборщик метрик для фоновых задач.
 * Позволяет видеть в Prometheus/Grafana:
 * - сколько напоминаний отправлено
 * - сколько задач упало
 * - время выполнения каждой задачи
 *
 * Все метрики инициализируются в конструкторе (без @PostConstruct).
 */
@Component
public class SchedulingMetricsCollector {

    private final MeterRegistry meterRegistry;

    // Все поля final — инициализируются в конструкторе
    private final Counter reminders24hSent;
    private final Counter reminders2hSent;
    private final Counter remindersFailed;
    private final Counter pdnDeletionsProcessed;
    private final Counter archiveProcessed;
    private final Timer jobDurationTimer;

    public SchedulingMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // ✅ Инициализация всех метрик прямо в конструкторе
        this.reminders24hSent = Counter.builder("priyut_scheduling_reminders_24h_sent_total")
                .description("Total 24-hour reminders sent")
                .register(meterRegistry);

        this.reminders2hSent = Counter.builder("priyut_scheduling_reminders_2h_sent_total")
                .description("Total 2-hour reminders sent")
                .register(meterRegistry);

        this.remindersFailed = Counter.builder("priyut_scheduling_reminders_failed_total")
                .description("Total failed reminder attempts")
                .register(meterRegistry);

        this.pdnDeletionsProcessed = Counter.builder("priyut_scheduling_pdn_deletions_total")
                .description("Total PDN deletion requests processed")
                .register(meterRegistry);

        this.archiveProcessed = Counter.builder("priyut_scheduling_archive_total")
                .description("Total volunteers archived")
                .register(meterRegistry);

        this.jobDurationTimer = Timer.builder("priyut_scheduling_job_duration_seconds")
                .description("Duration of scheduled job execution")
                .tag("job", "unknown")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    // === Методы для инкремента счётчиков ===

    public void recordReminder24hSent() {
        reminders24hSent.increment();
    }

    public void recordReminder2hSent() {
        reminders2hSent.increment();
    }

    public void recordReminderFailed() {
        remindersFailed.increment();
    }

    public void recordPdnDeletion() {
        pdnDeletionsProcessed.increment();
    }

    public void recordArchive() {
        archiveProcessed.increment();
    }

    /**
     * Возвращает таймер для конкретной задачи.
     * Создаёт новый таймер с тегом job=<имя_задачи>, если его ещё нет.
     */
    public Timer getJobDurationTimer(String jobName) {
        return Timer.builder("priyut_scheduling_job_duration_seconds")
                .description("Duration of scheduled job execution")
                .tag("job", jobName)
                .register(meterRegistry);
    }
}