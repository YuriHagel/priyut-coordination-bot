package ru.priyut.coordination.infrastructure.persistence.mapper;

import ru.priyut.coordination.domain.consent.Consent;
import ru.priyut.coordination.domain.consent.ConsentVersion;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.persistence.record.ConsentRecord;

/**
 * Маппер между доменным объектом Consent и ConsentRecord.
 */
public final class ConsentMapper {

    private ConsentMapper() {
    }

    public static ConsentRecord toRecord(Consent consent) {
        return new ConsentRecord(
                consent.getTelegramId().value(),
                consent.getCurrentVersion().version(),
                consent.getCurrentVersion().documentName(),
                consent.getGrantedAt(),
                consent.getWithdrawnAt(),
                consent.getWithdrawalReason(),
                consent.isActive()
        );
    }

    public static Consent toDomain(ConsentRecord record) {
        var version = ConsentVersion.of(record.consentVersion(), record.consentDocumentName());
        return Consent.reconstitute(
                TelegramId.of(record.telegramId()),
                version,
                record.grantedAt(),
                record.withdrawnAt(),
                record.withdrawalReason(),
                record.active()
        );
    }
}