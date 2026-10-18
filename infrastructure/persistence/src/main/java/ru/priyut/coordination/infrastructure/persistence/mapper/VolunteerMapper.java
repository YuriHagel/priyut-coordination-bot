package ru.priyut.coordination.infrastructure.persistence.mapper;

import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.volunteer.*;
import ru.priyut.coordination.infrastructure.persistence.record.VolunteerRecord;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;

/**
 * Маппер между доменным объектом Volunteer и VolunteerRecord.
 */
public final class VolunteerMapper {

    private VolunteerMapper() {
    }

    public static VolunteerRecord toRecord(Volunteer volunteer) {
        var availability = volunteer.getAvailability();
        var daysString = availability.daysOfWeek().stream()
                .map(Enum::name)
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        return new VolunteerRecord(
                volunteer.getId().value(),
                volunteer.getTelegramId().value(),
                volunteer.getFullName(),
                volunteer.getPhone().value(),
                volunteer.getDistrict().name(),
                volunteer.getCar().brand(),
                volunteer.getCar().model(),
                volunteer.getCar().color(),
                daysString,
                availability.timeFrom(),
                availability.timeTo(),
                availability.status().name(),
                volunteer.getStatus().name(),
                volunteer.getCreatedAt(),
                volunteer.getUpdatedAt(),
                volunteer.getLastActivityAt()
        );
    }

    public static Volunteer toDomain(VolunteerRecord record) {
        var days = parseDays(record.availabilityDays());
        var availability = Availability.of(
                days,
                record.availabilityTimeFrom(),
                record.availabilityTimeTo(),
                Availability.AvailabilityStatus.valueOf(record.availabilityStatus())
        );

        return Volunteer.reconstitute(
                VolunteerId.of(record.id()),
                TelegramId.of(record.telegramId()),
                VolunteerStatus.valueOf(record.status()),
                record.fullName(),
                PhoneNumber.of(record.phone()),
                District.of(record.district()),
                Car.of(record.carBrand(), record.carModel(), record.carColor()),
                availability,
                record.createdAt(),
                record.updatedAt(),
                record.lastActivityAt()
        );
    }

    private static Set<DayOfWeek> parseDays(String daysString) {
        if (daysString == null || daysString.isEmpty()) {
            return EnumSet.noneOf(DayOfWeek.class);
        }
        var days = EnumSet.noneOf(DayOfWeek.class);
        for (String day : daysString.split(",")) {
            days.add(DayOfWeek.valueOf(day.trim()));
        }
        return days;
    }
}