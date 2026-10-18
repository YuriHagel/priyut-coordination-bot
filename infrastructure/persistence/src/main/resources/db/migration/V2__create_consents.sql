CREATE TABLE consents
(
    telegram_id           BIGINT PRIMARY KEY,
    consent_version       VARCHAR(20)              NOT NULL,
    consent_document_name VARCHAR(200)             NOT NULL,
    granted_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    withdrawn_at          TIMESTAMP WITH TIME ZONE,
    withdrawal_reason     VARCHAR(500),
    active                BOOLEAN                  NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_consents_active ON consents (active);