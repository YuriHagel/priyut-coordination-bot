package ru.priyut.coordination.application.port.outbound;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.io.InputStream;
import java.util.List;

/**
 * Порт для отправки сообщений и документов в Telegram.
 * Реализуется в infrastructure-слое.
 */
public interface TelegramGateway {

    /**
     * Отправка простого текстового сообщения.
     */
    void sendMessage(TelegramId telegramId, String text);

    /**
     * Отправка текстового сообщения с клавиатурой (Inline или Reply).
     */
    void sendMessage(TelegramId telegramId, String text, ReplyKeyboard replyMarkup);

    /**
     * Отправка документа (например, PDF с политикой ПДн).
     *
     * @param telegramId      ID получателя
     * @param documentStream  Поток данных файла (например, из ClassPathResource)
     * @param fileName        Имя файла, которое увидит пользователь
     * @param caption         Подпись к документу
     */
    void sendDocument(TelegramId telegramId, InputStream documentStream, String fileName, String caption);

    /**
     * Массовая рассылка сообщения списку получателей.
     * ⚠️ В production этот метод должен быть обёрнут в RateLimiter (Resilience4j),
     * чтобы не превысить лимит Telegram (30 msg/sec).
     */
    void broadcastMessage(String text, List<TelegramId> recipients);
}