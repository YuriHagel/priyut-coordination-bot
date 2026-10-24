package ru.priyut.coordination.infrastructure.scheduling.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Конфигурация планировщика фоновых задач.
 * <p>
 * Выделена в отдельный класс, чтобы:
 * - Не засорять главный класс приложения
 * - Легко отключить планировщик в тестах (через @MockBean или профиль)
 * - Настроить пул потоков для параллельного выполнения задач
 */
@Configuration
public class SchedulingConfig {
    private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);

    /**
     * Пул потоков для фоновых задач.
     * Размер = количество одновременно выполняемых задач.
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3); // 3 задачи максимум параллельно
        scheduler.setThreadNamePrefix("scheduling-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setErrorHandler(throwable ->
                log.error("Unexpected error occurred in scheduled task execution", throwable)
        );
        return scheduler;
    }
}