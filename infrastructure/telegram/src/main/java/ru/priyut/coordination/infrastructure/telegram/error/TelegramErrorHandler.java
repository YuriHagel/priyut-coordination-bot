package ru.priyut.coordination.infrastructure.telegram.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

/**
 * Централизованный обработчик ошибок Telegram API.
 * Логирует ошибки, классифицирует их и преобразует в понятные исключения.
 */
@Component
public class TelegramErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(TelegramErrorHandler.class);

    /**
     * Обрабатывает исключение от Telegram API.
     *
     * @param e Исключение
     * @param context Контекст (например, "sending message to user 123")
     */
    public void handle(TelegramApiException e, String context) {
        if (e instanceof TelegramApiRequestException apiEx) {
            int errorCode = apiEx.getErrorCode();
            String description = apiEx.getDescription();

            if (errorCode == 429) {
                log.warn("⚠️ Telegram Rate Limit (429) hit: {} | Context: {}", description, context);
                // RetryPolicy сам обработает повторные попытки, но если они исчерпаны,
                // будет выброшено MaxRetriesExceededException, которое мы мапим ниже.
            } else if (errorCode >= 500) {
                log.error("❌ Telegram Server Error ({}): {} | Context: {}", errorCode, description, context);
            } else if (errorCode == 400) {
                log.error("❌ Telegram Bad Request (400): {} | Context: {}", description, context);
                // 400 обычно означает ошибку в наших данных (неверный chat_id, битый markdown).
                // Повторять бессмысленно.
            } else {
                log.error("❌ Telegram API Error ({}): {} | Context: {}", errorCode, description, context);
            }
        } else {
            log.error("❌ Unknown Telegram API Exception: {} | Context: {}", e.getMessage(), context, e);
        }
    }
}