package ru.priyut.coordination.infrastructure.telegram.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.in.CompleteTripUseCase;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGatewayImpl;

/**
 * Обработчик callback "✅ Закрыть поездку" (сценарий Е ТЗ).
 */
@Component
public class CloseTripHandler implements AdminCallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(CloseTripHandler.class);

    private final CompleteTripUseCase completeTripUseCase;
    private final TelegramBotGatewayImpl telegramGateway;

    public CloseTripHandler(
            CompleteTripUseCase completeTripUseCase,
            TelegramBotGatewayImpl telegramGateway
    ) {
        this.completeTripUseCase = completeTripUseCase;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String callbackPrefix() {
        return "trip:close:";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        var callbackData = update.getCallbackQuery().getData();
        var tripIdStr = callbackData.substring(callbackPrefix().length());

        try {
            var tripId = Long.parseLong(tripIdStr);
            completeTripUseCase.closeByAdmin(tripId, telegramId.value());
            log.info("Admin {} closed trip {}", telegramId, tripId);

            var msg = SendMessage.builder()
                    .chatId(String.valueOf(telegramId.value()))
                    .text("✅ Поездка #" + tripId + " закрыта.\nДанные сохранены в историю.")
                    .build();
            telegramGateway.execute(msg);
        } catch (Exception e) {
            log.error("Failed to close trip {} by admin {}", tripIdStr, telegramId, e);
        }
    }
}