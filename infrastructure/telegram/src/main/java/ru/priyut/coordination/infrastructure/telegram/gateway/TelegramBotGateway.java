package ru.priyut.coordination.infrastructure.telegram.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.bot.PriyutTelegramBot;

import java.io.InputStream;
import java.util.List;

/**
 * Реализация порта TelegramGateway.
 * Делегирует вызовы обёртке PriyutTelegramBot, которая логирует ошибки и упрощает тестирование.
 */
@Component
public class TelegramBotGateway implements TelegramGateway {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotGateway.class);

    private final PriyutTelegramBot telegramBot;

    public TelegramBotGateway(PriyutTelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void sendMessage(TelegramId telegramId, String text) {
        sendMessage(telegramId, text, null);
    }

    @Override
    public void sendMessage(TelegramId telegramId, String text, ReplyKeyboard replyMarkup) {
        try {
            // Используем Builder, как показано в декомпилированном коде SendMessage.
            // Это безопасно и позволяет легко добавлять опциональные поля.
            SendMessage message = SendMessage.builder()
                    .chatId(telegramId.value().toString()) // Telegram API требует String или Long
                    .text(text)
                    .replyMarkup(replyMarkup)
                    .build();

            // Опционально: можно включить Markdown для форматирования текста
            // message.enableMarkdownV2(true);

            telegramBot.execute(message);

        } catch (Exception e) {
            log.error("❌ Failed to send message to user {}: {}", telegramId, e.getMessage());
            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }

    @Override
    public void sendDocument(TelegramId telegramId, InputStream documentStream, String fileName, String caption) {
        try {
            // InputFile может принимать InputStream, что идеально для файлов из classpath (resources)
            InputFile inputFile = new InputFile(documentStream, fileName);

            SendDocument document = SendDocument.builder()
                    .chatId(telegramId.value().toString())
                    .document(inputFile)
                    .caption(caption)
                    .build();

            telegramBot.execute(document);

        } catch (Exception e) {
            log.error("❌ Failed to send document '{}' to user {}: {}", fileName, telegramId, e.getMessage());
            throw new RuntimeException("Failed to send Telegram document", e);
        }
    }

    @Override
    public void broadcastMessage(String text, List<TelegramId> recipients) {
        log.info("📢 Starting broadcast to {} recipients", recipients.size());
        int successCount = 0;
        int failCount = 0;

        for (TelegramId recipient : recipients) {
            try {
                sendMessage(recipient, text);
                successCount++;

                // Небольшая пауза для снижения нагрузки на API.
                // В production лучше использовать Resilience4j RateLimiter, как мы обсуждали ранее.
                Thread.sleep(50);
            } catch (Exception e) {
                failCount++;
                log.error("❌ Failed to broadcast to user {}: {}", recipient, e.getMessage());
                // Не прерываем цикл, чтобы отправить сообщение остальным
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Broadcast interrupted");
                throw new RuntimeException("Broadcast interrupted", e);
            }
        }

        log.info("✅ Broadcast completed. Success: {}, Failed: {}", successCount, failCount);
    }
}