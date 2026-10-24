package ru.priyut.coordination.infrastructure.telegram.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Маршрутизатор callback-запросов.
 * Находит подходящий CallbackHandler по префиксу callback_data.
 */
@Component
public class CallbackRouter {

    private static final Logger log = LoggerFactory.getLogger(CallbackRouter.class);

    private final Map<String, CallbackHandler> handlersByPrefix;
    private final TelegramGateway telegramGateway; // ✅ Внедряем интерфейс

    public CallbackRouter(List<CallbackHandler> handlers, TelegramGateway telegramGateway) {
        this.handlersByPrefix = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        CallbackHandler::callbackPrefix,
                        Function.identity()
                ));
        this.telegramGateway = telegramGateway;

        log.info("✅ CallbackRouter initialized with {} handlers: {}",
                handlers.size(), handlersByPrefix.keySet());
    }

    public void handle(Update update) {
        if (!update.hasCallbackQuery()) {
            return;
        }

        var callbackQuery = update.getCallbackQuery();
        var callbackData = callbackQuery.getData();
        var callbackQueryId = callbackQuery.getId();
        var telegramId = TelegramId.of(callbackQuery.getFrom().getId());

        try {
            var decoded = CallbackDataCodec.decode(callbackData);
            var handler = handlersByPrefix.get(decoded.action());

            if (handler == null) {
                log.warn("Unknown callback action '{}' from user {}", decoded.action(), telegramId);
                telegramGateway.answerCallbackQuery(callbackQueryId, "Неизвестное действие", true);
                return;
            }

            log.debug("Routing callback '{}' to {} for user {}",
                    decoded.action(), handler.getClass().getSimpleName(), telegramId);

            handler.handle(update, telegramId);

        } catch (Exception e) {
            log.error("Error handling callback '{}' from user {}", callbackData, telegramId, e);
            telegramGateway.answerCallbackQuery(callbackQueryId, "❌ Произошла ошибка", true);
        }
    }
}