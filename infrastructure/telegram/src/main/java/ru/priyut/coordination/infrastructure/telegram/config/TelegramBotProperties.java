package ru.priyut.coordination.infrastructure.telegram.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Конфигурация Telegram-бота.
 *
 * Пример application.yaml:
 * <pre>
 * priyut:
 *   telegram:
 *     bot:
 *       token: ${BOT_TOKEN}
 *       username: priyut_coordination_bot
 *       api-url: https://api.telegram.org
 *       admin-telegram-ids: 123456789, 987654321
 *     retry:
 *       max-attempts: 3
 *       wait-duration: 2s
 *     rate-limit:
 *       messages-per-second: 30
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "priyut.telegram")
public class TelegramBotProperties {

    @NotNull
    private final Bot bot = new Bot();

    @NotNull
    private final Retry retry = new Retry();

    @NotNull
    private final RateLimit rateLimit = new RateLimit();

    @NotNull
    private final Polling polling = new Polling();

    // === Getters ===

    public Bot getBot() { return bot; }
    public Retry getRetry() { return retry; }
    public RateLimit getRateLimit() { return rateLimit; }
    public Polling getPolling() { return polling; }

    // === Вложенные классы конфигурации ===

    public static class Bot {
        /**
         * Токен бота. Получается у @BotFather.
         * ⚠️ НИКОГДА не коммитить в репозиторий! Использовать env переменную.
         */
        @NotBlank
        private String token;

        /**
         * Username бота (без @).
         */
        @NotBlank
        private String username;

        /**
         * URL Telegram API. По умолчанию: https://api.telegram.org
         * Переопределяется в тестах на http://localhost:PORT (WireMock).
         */
        private String apiUrl = "https://api.telegram.org";

        /**
         * Белый список Telegram ID администраторов.
         * Только эти пользователи могут выполнять админские команды.
         */
        @NotEmpty
        private List<Long> adminTelegramIds = List.of();

        /**
         * Включить/выключить бота (для локальной разработки без реального Telegram).
         */
        private boolean enabled = true;

        // Getters/Setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

        public List<Long> getAdminTelegramIds() { return adminTelegramIds; }
        public void setAdminTelegramIds(List<Long> adminTelegramIds) {
            this.adminTelegramIds = adminTelegramIds;
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        /**
         * Удобный метод для получения Set админских ID (для быстрой проверки).
         */
        public Set<Long> getAdminIdsAsSet() {
            return adminTelegramIds.stream().collect(Collectors.toUnmodifiableSet());
        }
    }

    public static class Retry {
        /**
         * Максимальное количество попыток при ошибке Telegram API.
         */
        private int maxAttempts = 3;

        /**
         * Время ожидания между попытками.
         */
        private Duration waitDuration = Duration.ofSeconds(2);

        /**
         * Множитель для экспоненциального backoff.
         */
        private double backoffMultiplier = 2.0;

        // Getters/Setters
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

        public Duration getWaitDuration() { return waitDuration; }
        public void setWaitDuration(Duration waitDuration) { this.waitDuration = waitDuration; }

        public double getBackoffMultiplier() { return backoffMultiplier; }
        public void setBackoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
        }
    }

    public static class RateLimit {
        /**
         * Лимит сообщений в секунду (Telegram ограничивает до 30 msg/sec для бота).
         */
        private int messagesPerSecond = 30;

        /**
         * Таймаут ожидания, если лимит исчерпан.
         */
        private Duration timeoutDuration = Duration.ofSeconds(5);

        // Getters/Setters
        public int getMessagesPerSecond() { return messagesPerSecond; }
        public void setMessagesPerSecond(int messagesPerSecond) {
            this.messagesPerSecond = messagesPerSecond;
        }

        public Duration getTimeoutDuration() { return timeoutDuration; }
        public void setTimeoutDuration(Duration timeoutDuration) {
            this.timeoutDuration = timeoutDuration;
        }
    }

    public static class Polling {
        /**
         * Таймаут long polling (сколько секунд бот ждёт обновлений).
         */
        private int timeoutSeconds = 50;

        /**
         * Максимальное количество обновлений за один запрос.
         */
        private int updatesLimit = 100;

        /**
         * Разрешить обновления только определённых типов (оптимизация).
         */
        private List<String> allowedUpdates = List.of("message", "callback_query");

        // Getters/Setters
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

        public int getUpdatesLimit() { return updatesLimit; }
        public void setUpdatesLimit(int updatesLimit) { this.updatesLimit = updatesLimit; }

        public List<String> getAllowedUpdates() { return allowedUpdates; }
        public void setAllowedUpdates(List<String> allowedUpdates) {
            this.allowedUpdates = allowedUpdates;
        }
    }
}