package ru.priyut.coordination.infrastructure.telegram.admin;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.domain.shared.TelegramId;

/**
 * Маркерный интерфейс для callback-обработчиков, требующих прав админа.
 * <p>
 * Эти handler'ы срабатывают по inline-кнопкам (например, "✅ Подтвердить волонтёра"),
 * а не по слеш-командам.
 */
public interface AdminCallbackHandler {

    /**
     * Префикс callback_data, который обрабатывает этот handler.
     * Например: "volunteer:approve"
     */
    String callbackPrefix();

    /**
     * Обработка callback-запроса.
     */
    void handle(Update update, TelegramId telegramId);
}