package ru.priyut.coordination.infrastructure.telegram.middleware;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Цепочка обработки обновления.
 * Позволяет middleware передавать управление следующему обработчику.
 */
public interface UpdateChain {

    /**
     * Передать управление следующему middleware или финальному обработчику.
     */
    void proceed(Update update);
}