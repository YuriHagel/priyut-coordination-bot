package ru.priyut.coordination.infrastructure.persistence.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.priyut.coordination.domain.consent.Consent;
import ru.priyut.coordination.domain.consent.ConsentAuditEntry;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.persistence.record.ConsentAuditRecord;
import ru.priyut.coordination.infrastructure.persistence.record.ConsentRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcConsentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcConsentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Consent> findByTelegramId(TelegramId telegramId) {
        var sql = "SELECT * FROM consents WHERE telegram_id = :telegramId";
        var params = new MapSqlParameterSource().addValue("telegramId", telegramId.value());
        var records = jdbcTemplate.query(sql, params, new ConsentRowMapper());
        return records.stream().findFirst(); // Упрощённо, без маппера
    }

    public Consent save(Consent consent) {
        var sql = """
                INSERT INTO consents (
                    telegram_id, consent_version, consent_document_name,
                    granted_at, withdrawn_at, withdrawal_reason, active
                ) VALUES (
                    :telegramId, :consentVersion, :consentDocumentName,
                    :grantedAt, :withdrawnAt, :withdrawalReason, :active
                )
                ON CONFLICT (telegram_id) DO UPDATE SET
                    consent_version = EXCLUDED.consent_version,
                    consent_document_name = EXCLUDED.consent_document_name,
                    granted_at = EXCLUDED.granted_at,
                    withdrawn_at = EXCLUDED.withdrawn_at,
                    withdrawal_reason = EXCLUDED.withdrawal_reason,
                    active = EXCLUDED.active
                """;

        var params = new MapSqlParameterSource()
                .addValue("telegramId", consent.getTelegramId().value())
                .addValue("consentVersion", consent.getCurrentVersion().version())
                .addValue("consentDocumentName", consent.getCurrentVersion().documentName())
                .addValue("grantedAt", consent.getGrantedAt())
                .addValue("withdrawnAt", consent.getWithdrawnAt())
                .addValue("withdrawalReason", consent.getWithdrawalReason())
                .addValue("active", consent.isActive());

        jdbcTemplate.update(sql, params);
        return consent;
    }

    public void saveAuditEntry(ConsentAuditEntry entry) {
        var sql = """
                INSERT INTO consent_audit (
                    id, telegram_id, action, consent_version, consent_document_name,
                    occurred_at, source, comment
                ) VALUES (
                    :id, :telegramId, :action, :consentVersion, :consentDocumentName,
                    :occurredAt, :source, :comment
                )
                """;

        var params = new MapSqlParameterSource()
                .addValue("id", entry.id())
                .addValue("telegramId", entry.telegramId().value())
                .addValue("action", entry.action().name())
                .addValue("consentVersion", entry.version() != null ? entry.version().version() : null)
                .addValue("consentDocumentName", entry.version() != null ? entry.version().documentName() : null)
                .addValue("occurredAt", entry.occurredAt())
                .addValue("source", entry.source())
                .addValue("comment", entry.comment());

        jdbcTemplate.update(sql, params);
    }

    public List<ConsentAuditEntry> findAuditEntriesByTelegramId(TelegramId telegramId) {
        var sql = "SELECT * FROM consent_audit WHERE telegram_id = :telegramId ORDER BY occurred_at DESC";
        var params = new MapSqlParameterSource().addValue("telegramId", telegramId.value());
        var records = jdbcTemplate.query(sql, params, new ConsentAuditRowMapper());
        return records.stream().map(this::toAuditEntry).toList();
    }

    private ConsentAuditEntry toAuditEntry(ConsentAuditRecord record) {
        var version = record.consentVersion() != null
                ? ru.priyut.coordination.domain.consent.ConsentVersion.of(
                record.consentVersion(),
                record.consentDocumentName() != null ? record.consentDocumentName() : ""
        )
                : null;

        return ConsentAuditEntry.of(
                record.telegramId() != null ? TelegramId.of(record.telegramId()) : null,
                ConsentAuditEntry.AuditAction.valueOf(record.action()),
                version,
                record.occurredAt(),
                record.source(),
                record.comment()
        );
    }

    private static class ConsentRowMapper implements RowMapper<ConsentRecord> {
        @Override
        public ConsentRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ConsentRecord(
                    rs.getLong("telegram_id"),
                    rs.getString("consent_version"),
                    rs.getString("consent_document_name"),
                    rs.getTimestamp("granted_at").toInstant(),
                    getInstantOrNull(rs, "withdrawn_at"),
                    rs.getString("withdrawal_reason"),
                    rs.getBoolean("active")
            );
        }

        private java.time.Instant getInstantOrNull(ResultSet rs, String column) throws SQLException {
            var timestamp = rs.getTimestamp(column);
            return timestamp != null ? timestamp.toInstant() : null;
        }
    }

    private static class ConsentAuditRowMapper implements RowMapper<ConsentAuditRecord> {
        @Override
        public ConsentAuditRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ConsentAuditRecord(
                    (UUID) rs.getObject("id"),
                    rs.getLong("telegram_id"),
                    rs.getString("action"),
                    rs.getString("consent_version"),
                    rs.getString("consent_document_name"),
                    rs.getTimestamp("occurred_at").toInstant(),
                    rs.getString("source"),
                    rs.getString("comment")
            );
        }
    }
}