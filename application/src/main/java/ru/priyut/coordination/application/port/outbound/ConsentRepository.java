package ru.priyut.coordination.application.port.outbound;

import ru.priyut.coordination.domain.consent.Consent;
import ru.priyut.coordination.domain.consent.ConsentAuditEntry;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.util.List;
import java.util.Optional;

/**
 * Порт для работы с согласиями на обработку ПДн.
 */
public interface ConsentRepository {

    Optional<Consent> findByTelegramId(TelegramId telegramId);

    Consent save(Consent consent);

    void saveAuditEntry(ConsentAuditEntry entry);

    List<ConsentAuditEntry> findAuditEntriesByTelegramId(TelegramId telegramId);
}