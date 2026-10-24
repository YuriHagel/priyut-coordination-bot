package ru.priyut.coordination.infrastructure.telegram.error;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.MaxRetriesExceededException;
import io.github.resilience4j.retry.Retry;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * Утилита для применения политик Resilience4j (Rate Limiting + Retry) к вызовам.
 * Инкапсулирует логику декорирования, делая код отправки сообщений чище.
 */
@Component
public class RetryPolicy {

    private final RateLimiter rateLimiter;
    private final Retry retry;

    public RetryPolicy(RateLimiter rateLimiter, Retry retry) {
        this.rateLimiter = rateLimiter;
        this.retry = retry;
    }

    /**
     * Выполняет действие с применением Rate Limiter и Retry.
     *
     * @param action Действие для выполнения
     * @param <T> Тип возвращаемого значения
     * @return Результат выполнения
     * @throws RateLimitException если лимит исчерпан и таймаут истек
     * @throws RuntimeException если исчерпаны попытки Retry
     */
    public <T> T execute(Callable<T> action) {
        // 1. Сначала применяем Rate Limiter (если нет токена, ждем или падаем)
        Callable<T> rateLimitedAction = RateLimiter.decorateCallable(rateLimiter, action);

        // 2. Затем применяем Retry (повторяем при 429/5xx)
        Callable<T> resilientAction = Retry.decorateCallable(retry, rateLimitedAction);

        try {
            return resilientAction.call();
        } catch (RequestNotPermitted e) {
            throw new RateLimitException("Rate limit exceeded and timeout reached", e);
        } catch (MaxRetriesExceededException e) {
            throw new RuntimeException("Max retry attempts exceeded for Telegram API call", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute Telegram API call", e);
        }
    }

    /**
     * Вариант для действий без возвращаемого значения (Runnable).
     */
    public void execute(Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }
}