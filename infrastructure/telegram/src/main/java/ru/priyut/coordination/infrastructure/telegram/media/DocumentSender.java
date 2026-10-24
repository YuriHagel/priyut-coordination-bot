package ru.priyut.coordination.infrastructure.telegram.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.io.IOException;
import java.io.InputStream;

/**
 * Сервис для отправки документов (PDF, изображения) из classpath (resources).
 */
@Component
public class DocumentSender {

    private static final Logger log = LoggerFactory.getLogger(DocumentSender.class);

    private final TelegramGateway telegramGateway;

    public DocumentSender(TelegramGateway telegramGateway) {
        this.telegramGateway = telegramGateway;
    }

    /**
     * Отправляет файл из classpath пользователю.
     *
     * @param telegramId      Получатель
     * @param classpathPath   Путь к файлу, например "docs/Politika_PDN_Priyut.pdf"
     * @param fileName        Имя файла, которое увидит пользователь в Telegram
     * @param caption         Подпись к документу
     */
    public void sendFromClasspath(TelegramId telegramId, String classpathPath, String fileName, String caption) {
        var resource = new ClassPathResource(classpathPath);

        if (!resource.exists()) {
            log.error("❌ Document not found in classpath: {}", classpathPath);
            telegramGateway.sendMessage(telegramId, "⚠️ Извините, техническая ошибка: документ не найден.");
            return;
        }

        // try-with-resources гарантирует закрытие потока даже при ошибке отправки
        try (InputStream inputStream = resource.getInputStream()) {
            telegramGateway.sendDocument(telegramId, inputStream, fileName, caption);
            log.debug("✅ Successfully sent document '{}' to user {}", fileName, telegramId);
        } catch (IOException e) {
            log.error("❌ Failed to read document from classpath: {}", classpathPath, e);
            telegramGateway.sendMessage(telegramId, "⚠️ Ошибка при чтении документа. Попробуйте позже.");
        } catch (Exception e) {
            log.error("❌ Failed to send document '{}' to user {}", fileName, telegramId, e);
            telegramGateway.sendMessage(telegramId, "⚠️ Ошибка при отправке документа. Попробуйте позже.");
        }
    }
}