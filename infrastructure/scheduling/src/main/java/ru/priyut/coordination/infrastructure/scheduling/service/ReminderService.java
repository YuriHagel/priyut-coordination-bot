package ru.priyut.coordination.infrastructure.scheduling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.application.port.outbound.TripRepository;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.shared.UtcClock;
import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.domain.trip.TripStatus;
import ru.priyut.coordination.domain.volunteer.Volunteer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис отправки напоминаний о поездках.
 *
 * ИДЕМПОТЕНТНОСТЬ:
 * - Перед отправкой проверяем флаги reminder24hSent / reminder2hSent в Trip
 * - После отправки устанавливаем эти флаги
 * - При повторном запуске задачи напоминание не отправится повторно
 */
@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    private final TripRepository tripRepository;
    private final VolunteerRepository volunteerRepository;
    private final TelegramGateway telegramGateway;
    private final UtcClock clock;
    private final SchedulingMetricsCollector metrics;

    public ReminderService(
            TripRepository tripRepository,
            VolunteerRepository volunteerRepository,
            TelegramGateway telegramGateway,
            UtcClock clock,
            SchedulingMetricsCollector metrics
    ) {
        this.tripRepository = tripRepository;
        this.volunteerRepository = volunteerRepository;
        this.telegramGateway = telegramGateway;
        this.clock = clock;
        this.metrics = metrics;
    }

    /**
     * Отправка напоминаний за 24 часа до поездки (сценарий 4.5 ТЗ).
     * Ищет поездки в окне [now+23h, now+25h], чтобы не пропустить из-за неточности расписания.
     */
    @Transactional
    public void send24HourReminders() {
        LocalDateTime now = LocalDateTime.now(UtcClock.userZone());
        LocalDateTime windowStart = now.plusHours(23);
        LocalDateTime windowEnd = now.plusHours(25);

        List<Trip> trips = tripRepository.findUpcomingTrips(windowStart, windowEnd);
        log.info("🔔 Found {} trips in 24h reminder window", trips.size());

        for (Trip trip : trips) {
            // ИДЕМПОТЕНТНОСТЬ: пропускаем, если уже отправляли
            if (trip.isReminder24hSent()) {
                log.debug("Trip {} already has 24h reminder sent, skipping", trip.getId());
                continue;
            }

            // Напоминания только для активных поездок с назначенным волонтёром
            if (trip.getStatus() != TripStatus.ASSIGNED || trip.getAssignedVolunteerId() == null) {
                continue;
            }

            try {
                sendReminder(trip, 24);

                // Устанавливаем флаг идемпотентности
                trip.markReminder24hSent(clock.now());
                tripRepository.save(trip);

                metrics.recordReminder24hSent();
                log.info("✅ Sent 24h reminder for trip {} to volunteer {}",
                        trip.getId(), trip.getAssignedVolunteerId());
            } catch (Exception e) {
                metrics.recordReminderFailed();
                log.error("❌ Failed to send 24h reminder for trip {}", trip.getId(), e);
            }
        }
    }

    /**
     * Отправка напоминаний за 2 часа до поездки (сценарий 4.5 ТЗ).
     */
    @Transactional
    public void send2HourReminders() {
        LocalDateTime now = LocalDateTime.now(UtcClock.userZone());
        LocalDateTime windowStart = now.plusHours(1).plusMinutes(30);
        LocalDateTime windowEnd = now.plusHours(2).plusMinutes(30);

        List<Trip> trips = tripRepository.findUpcomingTrips(windowStart, windowEnd);
        log.info("🔔 Found {} trips in 2h reminder window", trips.size());

        for (Trip trip : trips) {
            if (trip.isReminder2hSent()) {
                continue;
            }
            if (trip.getStatus() != TripStatus.ASSIGNED || trip.getAssignedVolunteerId() == null) {
                continue;
            }

            try {
                sendReminder(trip, 2);

                trip.markReminder2hSent(clock.now());
                tripRepository.save(trip);

                metrics.recordReminder2hSent();
                log.info("✅ Sent 2h reminder for trip {}", trip.getId());
            } catch (Exception e) {
                metrics.recordReminderFailed();
                log.error("❌ Failed to send 2h reminder for trip {}", trip.getId(), e);
            }
        }
    }

    private void sendReminder(Trip trip, int hoursBefore) {
        Volunteer volunteer = volunteerRepository.findById(trip.getAssignedVolunteerId())
                .orElseThrow(() -> new IllegalStateException(
                        "Volunteer " + trip.getAssignedVolunteerId() + " not found"));

        String text = String.format(
                "⏰ НАПОМИНАНИЕ (за %d часа)\n\n" +
                        "📋 Поездка #%d\n" +
                        "📍 %s\n" +
                        "🕐 %s\n" +
                        "🐾 %s\n\n" +
                        "Если вы не сможете поехать — нажмите кнопку ниже.",
                hoursBefore,
                trip.getId().value().getMostSignificantBits(),
                trip.getRoute().formatted(),
                trip.getTripDateTime(),
                trip.getTaskDescription()
        );

        telegramGateway.sendMessage(volunteer.getTelegramId(), text);
    }
}