package ru.priyut.coordination.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.priyut.coordination.application.command.CancelTripCommand;
import ru.priyut.coordination.application.command.CreateTripCommand;
import ru.priyut.coordination.application.exception.AdminNotAuthorizedException;
import ru.priyut.coordination.application.exception.TripNotFoundException;
import ru.priyut.coordination.application.port.in.*;
import ru.priyut.coordination.application.port.outbound.AdminWhitelistRepository;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.application.port.outbound.TripRepository;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.shared.UtcClock;
import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.domain.volunteer.VolunteerId;

@Service
@Transactional
public class TripCoordinationService implements
        CreateTripUseCase,
        RespondToTripUseCase,
        ConfirmVolunteerUseCase,
        CancelTripUseCase,
        CompleteTripUseCase {

    private final TripRepository tripRepository;
    private final AdminWhitelistRepository adminWhitelistRepository;
    private final TelegramGateway telegramGateway;
    private final UtcClock clock;

    public TripCoordinationService(
            TripRepository tripRepository,
            AdminWhitelistRepository adminWhitelistRepository,
            TelegramGateway telegramGateway,
            UtcClock clock
    ) {
        this.tripRepository = tripRepository;
        this.adminWhitelistRepository = adminWhitelistRepository;
        this.telegramGateway = telegramGateway;
        this.clock = clock;
    }

    @Override
    public Trip createDraft(CreateTripCommand command) {
        // Проверяем, что пользователь — админ
        requireAdmin(command.adminTelegramId());

        var trip = Trip.createDraft(
                command.adminTelegramId(),
                command.route(),
                command.tripDateTime(),
                command.taskDescription(),
                command.type(),
                command.comment(),
                clock.now()
        );

        return tripRepository.save(trip);
    }

    @Override
    public Trip publish(Long tripId, Long adminTelegramId) {
        requireAdmin(adminTelegramId);

        var trip = findTripOrThrow(tripId);
        trip.publish(clock.now());
        trip = tripRepository.save(trip);

        // TODO: Рассылка волонтёрам
        return trip;
    }

    @Override
    public Trip respond(Long tripId, VolunteerId volunteerId) {
        var trip = findTripOrThrow(tripId);
        trip.respondByVolunteer(volunteerId, clock.now());
        trip = tripRepository.save(trip);

        // TODO: Уведомление админа
        return trip;
    }

    @Override
    public Trip confirm(Long tripId, Long adminTelegramId) {
        requireAdmin(adminTelegramId);

        var trip = findTripOrThrow(tripId);
        trip.confirmVolunteer(adminTelegramId, clock.now());
        trip = tripRepository.save(trip);

        // TODO: Уведомление волонтёра и остальных
        return trip;
    }

    @Override
    public Trip reject(Long tripId, Long adminTelegramId) {
        requireAdmin(adminTelegramId);

        var trip = findTripOrThrow(tripId);
        trip.rejectVolunteer(clock.now());
        trip = tripRepository.save(trip);

        // TODO: Уведомление волонтёра
        return trip;
    }

    @Override
    public Trip cancelByVolunteer(CancelTripCommand command) {
        var trip = findTripOrThrow(command.tripId());
        trip.cancelByVolunteer(command.reason(), clock.now());
        trip = tripRepository.save(trip);

        // TODO: Уведомление админа
        return trip;
    }

    @Override
    public Trip cancelByAdmin(Long tripId, Long adminTelegramId, String reason) {
        requireAdmin(adminTelegramId);

        var trip = findTripOrThrow(tripId);
        trip.cancelByAdmin(adminTelegramId, reason, clock.now());
        trip = tripRepository.save(trip);

        // TODO: Уведомление волонтёра
        return trip;
    }

    @Override
    public Trip markCompletedByVolunteer(Long tripId) {
        var trip = findTripOrThrow(tripId);
        trip.markCompletedByVolunteer(clock.now());
        trip = tripRepository.save(trip);

        // TODO: Уведомление админа
        return trip;
    }

    @Override
    public Trip returnForRework(Long tripId) {
        var trip = findTripOrThrow(tripId);
        trip.returnForRework(clock.now());
        trip = tripRepository.save(trip);

        // TODO: Уведомление волонтёра
        return trip;
    }

    @Override
    public Trip closeByAdmin(Long tripId, Long adminTelegramId) {
        requireAdmin(adminTelegramId);

        var trip = findTripOrThrow(tripId);
        trip.closeByAdmin(adminTelegramId, clock.now());
        trip = tripRepository.save(trip);

        // TODO: Уведомление волонтёра
        return trip;
    }

    // === Вспомогательные методы ===

    private void requireAdmin(Long telegramId) {
        var whitelist = adminWhitelistRepository.loadAll();
        if (!whitelist.isAdmin(TelegramId.of(telegramId))) {
            throw new AdminNotAuthorizedException(telegramId);
        }
    }

    private Trip findTripOrThrow(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException(tripId));
    }
}