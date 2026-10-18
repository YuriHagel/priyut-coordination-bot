package ru.priyut.coordination.infrastructure.persistence.record;

import java.time.Instant;
import java.util.UUID;

/**
 * Record для маппинга ResultSet из таблицы consent_audit.
 */
public record ConsentAuditRecord(
        UUID id,
        long telegramId,
        String action,
        String consentVersion,
        String consentDocumentName,
        Instant occurredAt,
        String source,
        String comment
) {
}