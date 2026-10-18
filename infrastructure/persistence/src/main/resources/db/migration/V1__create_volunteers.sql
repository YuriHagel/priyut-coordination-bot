CREATE TABLE volunteers
(
    id                     UUID PRIMARY KEY,
    telegram_id            BIGINT                   NOT NULL UNIQUE,
    full_name              VARCHAR(100)             NOT NULL,
    phone                  VARCHAR(20)              NOT NULL,
    district               VARCHAR(100)             NOT NULL,
    car_brand              VARCHAR(50)              NOT NULL,
    car_model              VARCHAR(50)              NOT NULL,
    car_color              VARCHAR(50),
    availability_days      VARCHAR(50)              NOT NULL, -- например: "MONDAY,TUESDAY,WEDNESDAY"
    availability_time_from TIME                     NOT NULL,
    availability_time_to   TIME                     NOT NULL,
    availability_status    VARCHAR(30)              NOT NULL,
    status                 VARCHAR(30)              NOT NULL,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    last_activity_at       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_volunteers_telegram_id ON volunteers (telegram_id);
CREATE INDEX idx_volunteers_status ON volunteers (status);