package ru.priyut.coordination.infrastructure.scheduling.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.priyut.coordination.infrastructure.scheduling.lock.JobExecutionLock;
import ru.priyut.coordination.infrastructure.scheduling.service.ReminderService;
import ru.priyut.coordination.infrastructure.scheduling.service.SchedulingMetricsCollector;

/**
 * Фоновая задача отправки напоминаний о поездках.
 *
 * Запускается каждые 15 минут, но реально отправляет напоминания только тем,
 * у кого поездка через 24 или 2 часа (окно поиска ±30 минут).
 *
 * УСТОЙЧИВОСТЬ К ПЕРЕЗАПУСКУ:
 * - Если приложение перезапустилось, задача продолжит работать
 * - Идемпотентность обеспечивается флагами в Trip (reminder24hSent, reminder2hSent)
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
        if (!lock.tryLock(JOB_NAME)) {
            return;
        }

        var timer = metrics.getJobDurationTimer(JOB_NAME);
        try {
            log.debug("🕐 Starting trip reminder job");
            reminderService.send24HourReminders();
            reminderService.send2HourReminders();
            log.debug("✅ Trip reminder job completed");
        } catch (Exception e) {
            log.error("❌ Trip reminder job failed", e);
        } finally {
            timer.stop(metrics.getJobDurationTimer(JOB_NAME));
            lock.unlock(JOB_NAME);
        }
    }
}