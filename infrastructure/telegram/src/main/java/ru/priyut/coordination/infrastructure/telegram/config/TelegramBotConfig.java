package ru.priyut.coordination.infrastructure.telegram.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import ru.priyut.coordination.infrastructure.telegram.bot.UpdateDispatcher;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGatewayImpl;

import java.util.Set;

/**
 * Основная конфигурация Telegram-бота.
 *
 * Создаёт:
 * - Telegram-клиент (OkHttp)
 * - Long polling приложение
 * - Gateway для отправки сообщений
 *
 * Условие: бот включается только если priyut.telegram.bot.enabled=true (по умолчанию).
 * Для локальной разработки без Telegram можно отключить: priyut.telegram.bot.enabled=false
 */
@Configuration
@EnableConfigurationProperties(TelegramBotProperties.class)
@ConditionalOnProperty(prefix = "priyut.telegram.bot", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TelegramBotConfig {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotConfig.class);

    private final TelegramBotProperties properties;

    public TelegramBotConfig(TelegramBotProperties properties) {
        this.properties = properties;
    }

    /**
     * HTTP-клиент для Telegram API (OkHttp).
     * Поддерживает кастомный API URL (для тестов с WireMock).
     */
    @Bean
    public OkHttpTelegramClient telegramClient() {
        var botConfig = properties.getBot();
        log.info("Creating Telegram client for bot @{} with API URL: {}",
                botConfig.getUsername(), botConfig.getApiUrl());

        // Если API URL отличается от дефолтного (тесты), используем кастомный builder
        if (!"https://api.telegram.org".equals(botConfig.getApiUrl())) {
            return new OkHttpTelegramClient(
                    botConfig.getToken(),
                    botConfig.getApiUrl()  // Переопределённый URL для WireMock
            );
        }

        // Стандартный клиент
        return new OkHttpTelegramClient(botConfig.getToken());
    }

    /**
     * Long polling приложение для получения обновлений от Telegram.
     */
    @Bean
    public TelegramBotsLongPollingApplication telegramBotsApplication(
            OkHttpTelegramClient telegramClient,
            UpdateDispatcher updateDispatcher
    ) {
        var botConfig = properties.getBot();
        var pollingConfig = properties.getPolling();

        log.info("Starting Telegram long polling for bot @{}", botConfig.getUsername());

        try {
            var application = new TelegramBotsLongPollingApplication();

            // Регистрируем бота с long polling consumer
            application.registerBot(
                    botConfig.getToken(),
                    new LongPollingSingleThreadUpdateConsumer() {
                        @Override
                        public void consume(org.telegram.telegrambots.meta.api.objects.Update update) {
                            updateDispatcher.dispatch(update);
                        }
                    }
            );

            log.info("✅ Telegram bot @{} started successfully", botConfig.getUsername());
            return application;

        } catch (Exception e) {
            log.error("❌ Failed to start Telegram bot", e);
            throw new RuntimeException("Failed to start Telegram bot", e);
        }
    }

    /**
     * Gateway для отправки сообщений через Telegram API.
     * Реализует порт из application-слоя.
     */
    @Bean
    public TelegramBotGatewayImpl telegramBotGateway(
            OkHttpTelegramClient telegramClient,
            ru.priyut.coordination.infrastructure.telegram.notification.RateLimitedMessageSender rateLimitedSender
    ) {
        return new TelegramBotGatewayImpl(telegramClient, rateLimitedSender);
    }

    /**
     * Белый список администраторов (Set для быстрой проверки).
     */
    @Bean
    public Set<Long> adminTelegramIds() {
        var adminIds = properties.getBot().getAdminIdsAsSet();
        log.info("Loaded {} admin Telegram IDs: {}", adminIds.size(),
                adminIds.stream().map(this::maskTelegramId).toList());
        return adminIds;
    }

    /**
     * Маскирование Telegram ID для логов (безопасность).
     */
    private String maskTelegramId(Long telegramId) {
        if (telegramId == null) return "null";
        var idStr = telegramId.toString();
        if (idStr.length() <= 4) return "***";
        return idStr.substring(0, 2) + "***" + idStr.substring(idStr.length() - 2);
    }
}