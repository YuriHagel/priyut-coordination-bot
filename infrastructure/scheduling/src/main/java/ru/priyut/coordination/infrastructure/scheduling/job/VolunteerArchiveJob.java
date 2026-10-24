package ru.priyut.coordination.infrastructure.scheduling.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.shared.UtcClock;
import ru.priyut.coordination.domain.volunteer.Volunteer;
import ru.priyut.coordination.domain.volunteer.VolunteerStatus;
import ru.priyut.coordination.infrastructure.scheduling.lock.JobExecutionLock;
import ru.priyut.coordination.infrastructure.scheduling.service.SchedulingMetricsCollector;

import java.time.Duration;
import java.util.List;

/**
 * Фоновая задача архивации профилей после 3 лет неактивности.
 */
@Component
public class VolunteerArchiveJob {

    private static final Logger log = LoggerFactory.getLogger(VolunteerArchiveJob.class);
    private static final String JOB_NAME = "volunteer-archive";
    private static final Duration INACTIVITY_THRESHOLD = Duration.ofDays(365 * 3);

    private final VolunteerRepository volunteerRepository;
    private final JobExecutionLock lock;
    private final SchedulingMetricsCollector metrics;
    private final UtcClock clock;

    public VolunteerArchiveJob(
            VolunteerRepository volunteerRepository,
            JobExecutionLock lock,
            SchedulingMetricsCollector metrics,
            UtcClock clock
    ) {
        this.volunteerRepository = volunteerRepository;
        this.lock = lock;
        this.metrics = metrics;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 3 * * *") // Каждый день в 3:00 ночи
    public void execute() {
        if (!lock.tryLock(JOB_NAME)) {
            return;
        }

        var timer = metrics.getJobDurationTimer(JOB_NAME).sample();
        try {
            log.debug("🕐 Starting volunteer archive job");

            // Ищем активных волонтёров с последней активностью старше 3 лет
            List<Volunteer> candidates = List.of(
                    volunteerRepository.findByStatus(VolunteerStatus.ACTIVE).toArray(new Volunteer[0])
            );

            var now = clock.now();
            int archived = 0;

            for (Volunteer volunteer : candidates) {
                if (Duration.between(volunteer.getLastActivityAt(), now).compareTo(INACTIVITY_THRESHOLD) >= 0) {
                    try {
                        volunteer.archive(now);
                        volunteerRepository.save(volunteer);
                        metrics.recordArchive();
                        archived++;
                        log.info("📦 Archived volunteer {} (inactive since {})",
                                volunteer.getTelegramId(), volunteer.getLastActivityAt());
                    } catch (Exception e) {
                        log.error("❌ Failed to archive volunteer {}", volunteer.getTelegramId(), e);
                    }
                }
            }

            log.info("✅ Volunteer archive job completed. Archived: {}", archived);
        } catch (Exception e) {
            log.error("❌ Volunteer archive job failed", e);
        } finally {
            timer.stop(metrics.getJobDurationTimer(JOB_NAME));
            lock.unlock(JOB_NAME);
        }
    }
}