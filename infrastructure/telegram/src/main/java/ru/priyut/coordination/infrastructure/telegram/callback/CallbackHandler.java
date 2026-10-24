package ru.priyut.coordination.infrastructure.telegram.callback;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.domain.shared.TelegramId;

/**
 * Интерфейс обработчика inline-кнопок (callback_query).
 */
public interface CallbackHandler {

    /**
     * Префикс callback_data, за который отвечает этот обработчик.
     * Например: "trip:resp:"
     */
    String callbackPrefix();

    /**
     * Обработка нажатия на кнопку.
     */
    void handle(Update update, TelegramId telegramId);
}