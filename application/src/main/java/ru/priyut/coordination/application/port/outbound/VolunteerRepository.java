package ru.priyut.coordination.application.port.outbound;

import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.volunteer.Volunteer;
import ru.priyut.coordination.domain.volunteer.VolunteerId;
import ru.priyut.coordination.domain.volunteer.VolunteerStatus;

import java.util.List;
import java.util.Optional;

/**
 * Порт для работы с волонтёрами.
 */
public interface VolunteerRepository {

    Optional<Volunteer> findById(VolunteerId id);

    Optional<Volunteer> findByTelegramId(TelegramId telegramId);

    List<Volunteer> findByStatus(VolunteerStatus status);

    Volunteer save(Volunteer volunteer);

    void deleteById(VolunteerId id);
}