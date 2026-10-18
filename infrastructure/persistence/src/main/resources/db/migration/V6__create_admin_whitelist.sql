CREATE TABLE admin_whitelist
(
    telegram_id  BIGINT PRIMARY KEY,
    display_name VARCHAR(100)             NOT NULL,
    added_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    added_by     BIGINT
);

-- Добавляем тестового админа (опционально, можно удалить в production)
-- INSERT INTO admin_whitelist (telegram_id, display_name) VALUES (123456789, 'Test Admin');