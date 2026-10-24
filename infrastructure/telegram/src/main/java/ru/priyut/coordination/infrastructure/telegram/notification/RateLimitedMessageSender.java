package ru.priyut.coordination.infrastructure.telegram.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.bot.PriyutTelegramBot;
import ru.priyut.coordination.infrastructure.telegram.error.RetryPolicy;
import ru.priyut.coordination.infrastructure.telegram.error.TelegramErrorHandler;

/**
 * Отправляет сообщения через Telegram API с защитой от Rate Limit и ошибок.
 */
@Component
public class RateLimitedMessageSender {

    private static final Logger log = LoggerFactory.getLogger(RateLimitedMessageSender.class);

    private final PriyutTelegramBot telegramBot;
    private final RetryPolicy retryPolicy;
    private final TelegramErrorHandler errorHandler;

    public RateLimitedMessageSender(
            PriyutTelegramBot telegramBot,
            RetryPolicy retryPolicy,
            TelegramErrorHandler errorHandler
    ) {
        this.telegramBot = telegramBot;
        this.retryPolicy = retryPolicy;
        this.errorHandler = errorHandler;
    }

    /**
     * Безопасная отправка сообщения одному пользователю.
     */
    public void sendSafely(TelegramId telegramId, SendMessage message) {
        message.setChatId(telegramId.value().toString());
        String context = "sending message to user " + telegramId;

        retryPolicy.execute(() -> {
            try {
                telegramBot.execute(message);
                return null;
            } catch (Exception e) {
                errorHandler.handle((org.telegram.telegrambots.meta.exceptions.TelegramApiException) e, context);
                throw e; // Пробрасываем дальше, чтобы RetryPolicy мог повторить попытку
            }
        });
    }
}