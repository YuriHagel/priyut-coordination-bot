package ru.priyut.coordination.infrastructure.scheduling.job;

import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.priyut.coordination.infrastructure.scheduling.lock.JobExecutionLock;
import ru.priyut.coordination.infrastructure.scheduling.service.ReminderService;
import ru.priyut.coordination.infrastructure.scheduling.service.SchedulingMetricsCollector;

/**
 * Фоновая задача отправки напоминаний о поездках.
 * Запускается каждые 15 минут, но реально отправляет напоминания только тем,
 * у кого поездка через 24 или 2 часа (окно поиска ±30 минут).
 */
@Component
public class TripReminderJob {

    private static final Logger log = LoggerFactory.getLogger(TripReminderJob.class);
    private static final String JOB_NAME = "trip-reminder";

    private final ReminderService reminderService;
    private final JobExecutionLock lock;
    private final SchedulingMetricsCollector metrics;

    public TripReminderJob(
            ReminderService reminderService,
            JobExecutionLock lock,
            SchedulingMetricsCollector metrics
    ) {
        this.reminderService = reminderService;
        this.lock = lock;
        this.metrics = metrics;
    }

    @Scheduled(fixedRate = 900_000) // Каждые 15 минут
    public void execute() {
        // Если задача уже выполняется, просто выходим (не замеряем это как выполнение)
        if (!lock.tryLock(JOB_NAME)) {
            return;
        }

        // ✅ Используем timer.record() для чистого и безопасного замера времени
        Timer timer = metrics.getJobDurationTimer(JOB_NAME);

        timer.record(() -> {
            try {
                log.debug("🕐 Starting trip reminder job");
                reminderService.send24HourReminders();
                reminderService.send2HourReminders();
                log.debug("✅ Trip reminder job completed");
            } catch (Exception e) {
                log.error("❌ Trip reminder job failed", e);
            } finally {
                // Гарантированно освобождаем блокировку
                lock.unlock(JOB_NAME);
            }
        });
    }
}