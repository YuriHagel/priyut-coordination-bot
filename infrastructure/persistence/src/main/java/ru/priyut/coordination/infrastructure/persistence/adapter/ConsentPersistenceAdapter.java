package ru.priyut.coordination.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import ru.priyut.coordination.application.port.outbound.ConsentRepository;
import ru.priyut.coordination.domain.consent.Consent;
import ru.priyut.coordination.domain.consent.ConsentAuditEntry;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.persistence.repository.JdbcConsentRepository;

import java.util.List;
import java.util.Optional;

@Component
public class ConsentPersistenceAdapter implements ConsentRepository {

    private final JdbcConsentRepository jdbcRepository;

    public ConsentPersistenceAdapter(JdbcConsentRepository jdbcRepository) {
        this.jdbcRepository = jdbcRepository;
    }

    @Override
    public Optional<Consent> findByTelegramId(TelegramId telegramId) {
        return jdbcRepository.findByTelegramId(telegramId);
    }

    @Override
    public Consent save(Consent consent) {
        return jdbcRepository.save(consent);
    }

    @Override
    public void saveAuditEntry(ConsentAuditEntry entry) {
        jdbcRepository.saveAuditEntry(entry);
    }

    @Override
    public List<ConsentAuditEntry> findAuditEntriesByTelegramId(TelegramId telegramId) {
        return jdbcRepository.findAuditEntriesByTelegramId(telegramId);
    }
}