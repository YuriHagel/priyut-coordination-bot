package ru.priyut.coordination.infrastructure.telegram.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Конфигурация Resilience4j для защиты от сбоев Telegram API.
 *
 * Включает:
 * - Rate Limiter (защита от превышения лимита 30 msg/sec)
 * - Retry (повторные попытки при 429/5xx)
 * - Circuit Breaker (защита от каскадных сбоев при падении Telegram API)
 * - Автоматические метрики для Prometheus
 */
@Configuration
@EnableConfigurationProperties(TelegramBotProperties.class)
public class TelegramRetryConfig {

    private static final Logger log = LoggerFactory.getLogger(TelegramRetryConfig.class);

    private final TelegramBotProperties properties;
    private final MeterRegistry meterRegistry;

    public TelegramRetryConfig(TelegramBotProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Rate Limiter: ограничивает количество сообщений в секунду.
     * Telegram лимит: 30 msg/sec для одного бота.
     */
    @Bean
    public RateLimiter telegramRateLimiter() {
        var rateLimitConfig = properties.getRateLimit();

        var config = RateLimiterConfig.custom()
                .limitForPeriod(rateLimitConfig.getMessagesPerSecond())
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(rateLimitConfig.getTimeoutDuration())
                .build();

        var rateLimiter = RateLimiter.of("telegramSendMessage", config);

        // Регистрируем метрики для Prometheus
        TaggedRateLimiterMetrics.ofRateLimiterRegistry(
                RateLimiterRegistry.of(config)
        ).bindTo(meterRegistry);

        log.info("✅ Telegram Rate Limiter configured: {} msg/sec, timeout: {}",
                rateLimitConfig.getMessagesPerSecond(),
                rateLimitConfig.getTimeoutDuration());

        return rateLimiter;
    }

    /**
     * Retry: повторные попытки при ошибках Telegram API.
     * Повторяет при:
     * - 429 Too Many Requests
     * - 5xx Server Errors
     * - Timeout
     * - IOException (сетевые ошибки)
     */
    @Bean
    public Retry telegramRetry() {
        var retryConfig = properties.getRetry();

        var config = RetryConfig.<Object>custom()
                .maxAttempts(retryConfig.getMaxAttempts())
                .waitDuration(retryConfig.getWaitDuration())
                .intervalBiFunction((attemptNumber, either) -> {
                    // Экспоненциальный backoff: 2s, 4s, 8s, ...
                    long waitMillis = retryConfig.getWaitDuration().toMillis() *
                            (long) Math.pow(retryConfig.getBackoffMultiplier(), attemptNumber - 1);
                    return waitMillis;
                })
                .retryOnException(throwable -> {
                    // Повторяем при:
                    // - TelegramApiRequestException с кодом 429 или 5xx
                    // - TimeoutException
                    // - IOException (сетевые ошибки)
                    if (throwable instanceof TelegramApiRequestException apiEx) {
                        int errorCode = apiEx.getErrorCode();
                        return errorCode == 429 || (errorCode >= 500 && errorCode < 600);
                    }
                    return throwable instanceof TimeoutException
                            || throwable instanceof IOException
                            || throwable instanceof TelegramApiException;
                })
                .build();

        var retry = Retry.of("telegramRetry", config);

        // Регистрируем метрики для Prometheus
        TaggedRetryMetrics.ofRetryRegistry(
                RetryRegistry.of(config)
        ).bindTo(meterRegistry);

        // Логируем события retry
        retry.getEventPublisher()
                .onRetry(event -> log.warn(
                        "🔄 Telegram API retry attempt {}/{} due to: {}",
                        event.getNumberOfRetryAttempts(),
                        retryConfig.getMaxAttempts(),
                        event.getLastThrowable().getMessage()
                ))
                .onSuccess(event -> log.debug(
                        "✅ Telegram API call succeeded after {} attempts",
                        event.getNumberOfRetryAttempts()
                ))
                .onError(event -> log.error(
                        "❌ Telegram API call failed after {} attempts: {}",
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable().getMessage()
                ));

        log.info("✅ Telegram Retry configured: maxAttempts={}, waitDuration={}, backoffMultiplier={}",
                retryConfig.getMaxAttempts(),
                retryConfig.getWaitDuration(),
                retryConfig.getBackoffMultiplier());

        return retry;
    }

    /**
     * Circuit Breaker: защита от каскадных сбоев при падении Telegram API.
     *
     * Состояния:
     * - CLOSED: нормальная работа
     * - OPEN: Telegram API недоступен, все вызовы блокируются
     * - HALF_OPEN: проверяем, восстановился ли API
     *
     * Переход в OPEN, если:
     * - 50% запросов завершились ошибкой
     * - Минимум 10 запросов за sliding window
     *
     * Возврат в CLOSED после 30 секунд в HALF_OPEN с успешными запросами.
     */
    @Bean
    public CircuitBreaker telegramCircuitBreaker() {
        var config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)           // 50% ошибок → OPEN
                .slowCallRateThreshold(80.0f)          // 80% медленных вызовов → OPEN
                .slowCallDurationThreshold(Duration.ofSeconds(10))
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(5)
                .slidingWindowSize(20)                 // Sliding window: 20 запросов
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .minimumNumberOfCalls(10)              // Минимум 10 вызовов перед оценкой
                .recordExceptions(
                        TelegramApiException.class,
                        TimeoutException.class,
                        IOException.class
                )
                .build();

        var circuitBreaker = CircuitBreaker.of("telegramCircuitBreaker", config);

        // Регистрируем метрики для Prometheus
        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(
                CircuitBreakerRegistry.of(config)
        ).bindTo(meterRegistry);

        // Логируем переходы между состояниями
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.warn(
                        "⚡ Telegram Circuit Breaker state changed: {} → {}",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()
                ))
                .onCallNotPermitted(event -> log.error(
                        "🚫 Telegram API call not permitted (Circuit Breaker is OPEN)"
                ));

        log.info("✅ Telegram Circuit Breaker configured: failureRateThreshold=50%, waitDurationInOpenState=30s");

        return circuitBreaker;
    }
}