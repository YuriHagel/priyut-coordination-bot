package ru.priyut.coordination.infrastructure.telegram.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.util.List;

/**
 * Диспетчер для пакетной отправки сообщений.
 * Выполняется асинхронно, чтобы не блокировать основной поток обработки обновлений.
 */
@Component
public class BatchMessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(BatchMessageDispatcher.class);

    private final RateLimitedMessageSender rateLimitedSender;

    public BatchMessageDispatcher(RateLimitedMessageSender rateLimitedSender) {
        this.rateLimitedSender = rateLimitedSender;
    }

    /**
     * Асинхронная рассылка сообщения списку получателей.
     * RateLimitedMessageSender гарантирует, что мы не превысим 30 msg/sec.
     */
    @Async("telegramTaskExecutor") // Требует настройки TaskExecutor в Spring Config
    public void dispatchBatch(List<TelegramId> recipients, SendMessage messageTemplate) {
        log.info("📢 Starting async batch dispatch to {} recipients", recipients.size());

        int successCount = 0;
        int failCount = 0;

        for (TelegramId recipient : recipients) {
            // Клонируем сообщение для каждого получателя (Telegram API требует уникальный chat_id)
            SendMessage message = SendMessage.builder()
                    .chatId(recipient.value().toString())
                    .text(messageTemplate.getText())
                    .parseMode(messageTemplate.getParseMode())
                    .replyMarkup(messageTemplate.getReplyMarkup())
                    .build();

            try {
                rateLimitedSender.sendSafely(recipient, message);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("❌ Failed to send batch message to user {}", recipient, e);
                // Не прерываем цикл, продолжаем рассылку остальным
            }
        }

        log.info("✅ Batch dispatch completed. Success: {}, Failed: {}", successCount, failCount);
    }
}