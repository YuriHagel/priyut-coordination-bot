package ru.priyut.coordination.domain.shared;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Порт для получения текущего времени.
 * В production используется системный UTC-часы,
 * в тестах — фиксированные часы для детерминированности.
 */
public interface UtcClock {

    Instant now();

    /**
     * Production-реализация: системные UTC-часы.
     */
    static UtcClock system() {
        Clock clock = Clock.system(ZoneOffset.UTC);
        return clock::instant;
    }

    /**
     * Тестовая реализация: фиксированное время.
     */
    static UtcClock fixed(Instant instant) {
        return () -> instant;
    }

    /**
     * Часовой пояс приложения (для отображения пользователю).
     */
    static ZoneId userZone() {
        return ZoneId.of("Europe/Moscow");
    }
}
