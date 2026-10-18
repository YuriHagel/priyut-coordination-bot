package ru.priyut.coordination.infrastructure.persistence.record;

import java.time.Instant;

/**
 * Record для маппинга ResultSet из таблицы consents.
 */
public record ConsentRecord(
        long telegramId,
        String consentVersion,
        String consentDocumentName,
        Instant grantedAt,
        Instant withdrawnAt,
        String withdrawalReason,
        boolean active
) {
}