CREATE TABLE trip_history
(
    id                BIGSERIAL PRIMARY KEY,
    trip_uuid         UUID                     NOT NULL,
    event_type        VARCHAR(50)              NOT NULL,
    actor_telegram_id BIGINT,
    payload           TEXT,
    occurred_at       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_trip_history_trip_uuid ON trip_history (trip_uuid);
CREATE INDEX idx_trip_history_occurred_at ON trip_history (occurred_at);