package ru.priyut.coordination.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Конфигурация для эмуляции Telegram Bot API в интеграционных тестах.
 * Запускает WireMock на случайном порту и перенаправляет туда HTTP-клиент приложения.
 */
@TestConfiguration
public class WireMockConfig {

    private static WireMockServer wireMockServer;

    static {
        // Инициализируем и запускаем WireMock на динамическом (свободном) порту (0)
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @Bean(destroyMethod = "stop")
    @Lazy
    public WireMockServer wireMockServer() {
        return wireMockServer;
    }

    /**
     * Динамически переопределяем свойства приложения перед загрузкой Spring Context.
     */
    @DynamicPropertySource
    static void registerWireMockProperties(DynamicPropertyRegistry registry) {
        // Заставляем приложение стучаться в локальный WireMock вместо реального Telegram API
        registry.add("priyut.telegram.api-url", () -> "http://localhost:" + wireMockServer.port());
        // Устанавливаем тестовый токен, чтобы он совпадал с тем, что ожидает WireMock (если нужно)
        registry.add("priyut.telegram.bot.token", () -> "TEST_TOKEN");
    }
}
