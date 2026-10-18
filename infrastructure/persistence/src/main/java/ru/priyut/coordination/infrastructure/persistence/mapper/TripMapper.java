package ru.priyut.coordination.infrastructure.persistence.mapper;

import ru.priyut.coordination.domain.trip.*;
import ru.priyut.coordination.domain.volunteer.VolunteerId;
import ru.priyut.coordination.infrastructure.persistence.record.TripRecord;

import java.time.Instant;

/**
 * Маппер между доменным объектом Trip и TripRecord.
 */
public final class TripMapper {

    private TripMapper() {
    }

    public static TripRecord toRecord(Trip trip) {
        var assignment = trip.getAssignment();
        return new TripRecord(
                trip.getId().value().getMostSignificantBits(), // упрощённо для BIGSERIAL
                trip.getId().value(),
                trip.getCreatedByAdminTelegramId(),
                trip.getRoute().from(),
                trip.getRoute().to(),
                trip.getRoute().destination(),
                trip.getTripDateTime(),
                trip.getTaskDescription(),
                trip.getType().name(),
                trip.getComment().value(),
                trip.getStatus().name(),
                assignment != null ? assignment.volunteerId().value() : null,
                assignment != null ? assignment.assignedByAdminTelegramId() : null,
                assignment != null ? assignment.assignedAt() : null,
                trip.getPublishedAt(),
                trip.getCancelledAt(),
                trip.getCancelReason(),
                trip.getCompletedAt(),
                trip.getClosedByAdminTelegramId(),
                trip.getCreatedAt(),
                trip.getUpdatedAt(),
                trip.getVersion()
        );
    }

    public static Trip toDomain(TripRecord record) {
        var route = Route.of(record.routeFrom(), record.routeTo(), record.routeDestination());
        var comment = TripComment.of(record.comment());
        var assignment = record.assignedVolunteerUuid() != null
                ? TripAssignment.of(
                VolunteerId.of(record.assignedVolunteerUuid()),
                record.assignedByAdminTelegramId(),
                record.assignedAt()
        )
                : null;

        return Trip.reconstitute(
                TripId.of(record.uuid()),
                record.createdByAdminTelegramId(),
                route,
                record.tripDateTime(),
                record.taskDescription(),
                TripType.valueOf(record.tripType()),
                comment,
                TripStatus.valueOf(record.status()),
                assignment,
                record.createdAt(),
                record.publishedAt(),
                record.cancelledAt(),
                record.cancelReason(),
                record.completedAt(),
                record.closedByAdminTelegramId(),
                record.updatedAt(),
                record.version()
        );
    }
}