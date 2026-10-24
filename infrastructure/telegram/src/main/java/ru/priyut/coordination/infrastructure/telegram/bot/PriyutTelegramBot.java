package ru.priyut.coordination.infrastructure.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

/**
 * Обёртка над Telegram API клиентом.
 * Предоставляет унифицированный интерфейс для отправки сообщений и документов.
 *
 * Зачем нужна обёртка:
 * - Упрощение тестирования (можно замокать)
 * - Централизованная обработка ошибок
 * - Логирование всех вызовов API
 * - Возможность добавить retry/circuit breaker в будущем
 */
@Component
public class PriyutTelegramBot {

    private static final Logger log = LoggerFactory.getLogger(PriyutTelegramBot.class);

    private final OkHttpTelegramClient telegramClient;

    public PriyutTelegramBot(OkHttpTelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * Отправка текстового сообщения.
     */
    public void execute(SendMessage message) {
        try {
            telegramClient.execute(message);
            log.debug("✅ Sent message to chat_id={}", message.getChatId());
        } catch (TelegramApiException e) {
            log.error("❌ Failed to send message to chat_id={}: {}",
                    message.getChatId(), e.getMessage());
            throw new RuntimeException("Failed to send message", e);
        }
    }

    /**
     * Отправка документа (PDF, изображение и т.д.).
     */
    public void execute(SendDocument document) {
        try {
            telegramClient.execute(document);
            log.debug("✅ Sent document to chat_id={}", document.getChatId());
        } catch (TelegramApiException e) {
            log.error("❌ Failed to send document to chat_id={}: {}",
                    document.getChatId(), e.getMessage());
            throw new RuntimeException("Failed to send document", e);
        }
    }

    /**
     * Выполнение произвольного API-метода.
     */
    public <T extends Serializable> T execute(BotApiMethod<T> method) {
        try {
            var result = telegramClient.execute(method);
            log.debug("✅ Executed API method: {}", method.getMethod());
            return result;
        } catch (TelegramApiException e) {
            log.error("❌ Failed to execute API method {}: {}",
                    method.getMethod(), e.getMessage());
            throw new RuntimeException("Failed to execute API method", e);
        }
    }

    /**
     * Получение файла по file_id.
     */
    public File getFile(String fileId) {
        try {
            var method = new org.telegram.telegrambots.meta.api.methods.get.GetFile();
            method.setFileId(fileId);
            return telegramClient.execute(method);
        } catch (TelegramApiException e) {
            log.error("❌ Failed to get file {}: {}", fileId, e.getMessage());
            throw new RuntimeException("Failed to get file", e);
        }
    }
}