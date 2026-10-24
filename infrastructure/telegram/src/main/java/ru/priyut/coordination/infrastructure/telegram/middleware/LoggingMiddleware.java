package ru.priyut.coordination.infrastructure.telegram.middleware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Middleware для логирования всех входящих обновлений.
 * Выполняется первым (порядок 10).
 *
 * Логирует:
 * - update_id
 * - тип обновления (message, callback_query)
 * - Telegram ID пользователя (маскированный)
 * - текст команды или callback_data
 */
@Component
public class LoggingMiddleware implements Middleware {

    private static final Logger log = LoggerFactory.getLogger(LoggingMiddleware.class);

    @Override
    public void handle(Update update, UpdateChain chain) {
        var updateId = update.getUpdateId();
        var type = getUpdateType(update);
        var telegramId = extractTelegramId(update);
        var details = extractDetails(update);

        log.info("📥 Telegram update: id={}, type={}, user={}, details={}",
                updateId, type, maskTelegramId(telegramId), details);

        // Передаём управление следующему middleware
        chain.proceed(update);
    }

    @Override
    public int order() {
        return 10; // Первый middleware
    }

    private String getUpdateType(Update update) {
        if (update.hasMessage() && update.getMessage().isCommand()) {
            return "COMMAND";
        }
        if (update.hasCallbackQuery()) {
            return "CALLBACK";
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            return "TEXT";
        }
        return "OTHER";
    }

    private Long extractTelegramId(Update update) {
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            return update.getMessage().getFrom().getId();
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            return update.getCallbackQuery().getFrom().getId();
        }
        return null;
    }

    private String extractDetails(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var text = update.getMessage().getText();
            return text.length() > 50 ? text.substring(0, 50) + "..." : text;
        }
        if (update.hasCallbackQuery()) {
            var data = update.getCallbackQuery().getData();
            return "callback_data=" + data;
        }
        return "no details";
    }

    private String maskTelegramId(Long telegramId) {
        if (telegramId == null) return "null";
        var idStr = telegramId.toString();
        if (idStr.length() <= 4) return "***";
        return idStr.substring(0, 2) + "***" + idStr.substring(idStr.length() - 2);
    }
}