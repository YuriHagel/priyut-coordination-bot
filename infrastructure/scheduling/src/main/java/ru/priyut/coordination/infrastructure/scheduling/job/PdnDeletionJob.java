package ru.priyut.coordination.infrastructure.scheduling.job;

import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.priyut.coordination.application.port.in.DeleteProfileUseCase;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.shared.UtcClock;
import ru.priyut.coordination.domain.volunteer.Volunteer;
import ru.priyut.coordination.domain.volunteer.VolunteerStatus;
import ru.priyut.coordination.infrastructure.scheduling.lock.JobExecutionLock;
import ru.priyut.coordination.infrastructure.scheduling.service.SchedulingMetricsCollector;

import java.time.Duration;
import java.util.List;

@Component
public class PdnDeletionJob {

    private static final Logger log = LoggerFactory.getLogger(PdnDeletionJob.class);
    private static final String JOB_NAME = "pdn-deletion";
    private static final Duration DELETION_DELAY = Duration.ofDays(3);

    private final VolunteerRepository volunteerRepository;
    private final DeleteProfileUseCase deleteProfileUseCase;
    private final JobExecutionLock lock;
    private final SchedulingMetricsCollector metrics;
    private final UtcClock clock;

    public PdnDeletionJob(
            VolunteerRepository volunteerRepository,
            DeleteProfileUseCase deleteProfileUseCase,
            JobExecutionLock lock,
            SchedulingMetricsCollector metrics,
            UtcClock clock
    ) {
        this.volunteerRepository = volunteerRepository;
        this.deleteProfileUseCase = deleteProfileUseCase;
        this.lock = lock;
        this.metrics = metrics;
        this.clock = clock;
    }

    @Scheduled(fixedRate = 3_600_000) // Каждый час
    public void execute() {
        if (!lock.tryLock(JOB_NAME)) {
            return;
        }

        // ✅ Получаем Timer
        Timer timer = metrics.getJobDurationTimer(JOB_NAME);

        // ✅ Оборачиваем всю логику в timer.record().
        // Micrometer сам измерит время и корректно обработает исключения.
        timer.record(() -> {
            try {
                log.debug("🕐 Starting PDN deletion job");
                List<Volunteer> pendingDeletions = volunteerRepository.findByStatus(VolunteerStatus.DELETION_REQUESTED);
                var now = clock.now();
                int processed = 0;

                for (Volunteer volunteer : pendingDeletions) {
                    if (Duration.between(volunteer.getUpdatedAt(), now).compareTo(DELETION_DELAY) >= 0) {
                        try {
                            deleteProfileUseCase.deletePersonalData(volunteer.getTelegramId());
                            metrics.recordPdnDeletion();
                            processed++;
                            log.info("✅ Deleted PDN for volunteer {}", volunteer.getTelegramId());
                        } catch (Exception e) {
                            log.error("❌ Failed to delete PDN for volunteer {}", volunteer.getTelegramId(), e);
                            // Не прерываем цикл, если один волонтер упал
                        }
                    }
                }
                log.info("✅ PDN deletion job completed. Processed: {}", processed);
            } catch (Exception e) {
                log.error("❌ PDN deletion job failed", e);
            } finally {
                // Освобождаем лок ПОСЛЕ завершения работы (время разблокировки ничтожно мало)
                lock.unlock(JOB_NAME);
            }
        });
    }
}