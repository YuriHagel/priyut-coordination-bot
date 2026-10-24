package ru.priyut.coordination.infrastructure.scheduling.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Защита от параллельного выполнения одной и той же задачи.
 *
 * Для MVP используем in-memory lock (подходит для одного инстанса).
 * Для production с несколькими инстансами — заменить на Redis/PostgreSQL lock.
 */
@Component
public class JobExecutionLock {

    private static final Logger log = LoggerFactory.getLogger(JobExecutionLock.class);
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * Пытается захватить блокировку для задачи.
     *
     * @return true, если блокировка захвачена (можно выполнять задачу).
     *         false, если задача уже выполняется (нужно пропустить этот запуск).
     */
    public boolean tryLock(String jobName) {
        ReentrantLock lock = locks.computeIfAbsent(jobName, k -> new ReentrantLock());
        boolean acquired = lock.tryLock();

        if (!acquired) {
            log.warn("🔒 Job '{}' is already running, skipping this execution", jobName);
        }

        return acquired;
    }

    /**
     * Освобождает блокировку после выполнения задачи.
     */
    public void unlock(String jobName) {
        ReentrantLock lock = locks.get(jobName);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}