package ru.priyut.coordination.infrastructure.telegram.error;

/**
 * Исключение, выбрасываемое, когда Resilience4j RateLimiter отклоняет запрос
 * из-за превышения лимита и истечения таймаута ожидания.
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}