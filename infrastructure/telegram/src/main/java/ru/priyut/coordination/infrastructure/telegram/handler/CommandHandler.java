package ru.priyut.coordination.infrastructure.telegram.handler;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.domain.shared.TelegramId;

/**
 * Интерфейс обработчика Telegram-команды (слеш-команды, начинающиеся с /).
 *
 * Примеры: /start, /new_trip, /my_trips, /profile и т.д.
 */
public interface CommandHandler {

    /**
     * Имя команды, которую обрабатывает этот handler.
     * Например: "/start", "/new_trip".
     */
    String command();

    /**
     * Описание команды для справки (опционально).
     */
    default String description() {
        return "";
    }

    /**
     * Обработка команды.
     *
     * @param update     входящее обновление от Telegram
     * @param telegramId ID пользователя, отправившего команду
     */
    void handle(Update update, TelegramId telegramId);
}