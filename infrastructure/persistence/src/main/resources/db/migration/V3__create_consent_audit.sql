CREATE TABLE consent_audit
(
    id                    UUID PRIMARY KEY,
    telegram_id           BIGINT                   NOT NULL,
    action                VARCHAR(50)              NOT NULL,
    consent_version       VARCHAR(20),
    consent_document_name VARCHAR(200),
    occurred_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    source                VARCHAR(100)             NOT NULL,
    comment               VARCHAR(500)
);

CREATE INDEX idx_consent_audit_telegram_id ON consent_audit (telegram_id);
CREATE INDEX idx_consent_audit_occurred_at ON consent_audit (occurred_at);