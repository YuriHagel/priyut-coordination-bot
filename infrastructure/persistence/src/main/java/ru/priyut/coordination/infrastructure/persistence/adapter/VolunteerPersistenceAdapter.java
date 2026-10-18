package ru.priyut.coordination.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.volunteer.Volunteer;
import ru.priyut.coordination.domain.volunteer.VolunteerId;
import ru.priyut.coordination.domain.volunteer.VolunteerStatus;
import ru.priyut.coordination.infrastructure.persistence.repository.JdbcVolunteerRepository;

import java.util.List;
import java.util.Optional;

@Component
public class VolunteerPersistenceAdapter implements VolunteerRepository {

    private final JdbcVolunteerRepository jdbcRepository;

    public VolunteerPersistenceAdapter(JdbcVolunteerRepository jdbcRepository) {
        this.jdbcRepository = jdbcRepository;
    }

    @Override
    public Optional<Volunteer> findById(VolunteerId id) {
        return jdbcRepository.findById(id);
    }

    @Override
    public Optional<Volunteer> findByTelegramId(TelegramId telegramId) {
        return jdbcRepository.findByTelegramId(telegramId);
    }

    @Override
    public List<Volunteer> findByStatus(VolunteerStatus status) {
        return jdbcRepository.findByStatus(status);
    }

    @Override
    public Volunteer save(Volunteer volunteer) {
        return jdbcRepository.save(volunteer);
    }

    @Override
    public void deleteById(VolunteerId id) {
        jdbcRepository.deleteById(id);
    }
}