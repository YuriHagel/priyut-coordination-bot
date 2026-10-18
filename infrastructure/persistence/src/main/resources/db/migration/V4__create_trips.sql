CREATE TABLE trips
(
    id                            BIGSERIAL PRIMARY KEY,
    uuid                          UUID                     NOT NULL UNIQUE,
    created_by_admin_telegram_id  BIGINT                   NOT NULL,
    route_from                    VARCHAR(200)             NOT NULL,
    route_to                      VARCHAR(200)             NOT NULL,
    route_destination             VARCHAR(200)             NOT NULL,
    trip_date_time                TIMESTAMP                NOT NULL, -- в Europe/Moscow
    task_description              VARCHAR(500)             NOT NULL,
    trip_type                     VARCHAR(20)              NOT NULL,
    comment                       VARCHAR(500),
    status                        VARCHAR(30)              NOT NULL,
    assigned_volunteer_uuid       UUID,
    assigned_by_admin_telegram_id BIGINT,
    assigned_at                   TIMESTAMP WITH TIME ZONE,
    published_at                  TIMESTAMP WITH TIME ZONE,
    cancelled_at                  TIMESTAMP WITH TIME ZONE,
    cancel_reason                 VARCHAR(500),
    completed_at                  TIMESTAMP WITH TIME ZONE,
    closed_by_admin_telegram_id   BIGINT,
    created_at                    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                    TIMESTAMP WITH TIME ZONE NOT NULL,
    version                       BIGINT                   NOT NULL DEFAULT 0
);

CREATE INDEX idx_trips_uuid ON trips (uuid);
CREATE INDEX idx_trips_status ON trips (status);
CREATE INDEX idx_trips_trip_date_time ON trips (trip_date_time);
CREATE INDEX idx_trips_assigned_volunteer ON trips (assigned_volunteer_uuid);