package ru.priyut.coordination.infrastructure.telegram.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.in.CompleteTripUseCase;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.domain.shared.TelegramId;

/**
 * Обработчик callback "✅ Закрыть поездку" (сценарий Е ТЗ).
 */
@Component
public class CloseTripHandler implements AdminCallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(CloseTripHandler.class);

    private final CompleteTripUseCase completeTripUseCase;
    private final TelegramGateway telegramGateway; // ✅ Внедряем интерфейс

    public CloseTripHandler(
            CompleteTripUseCase completeTripUseCase,
            TelegramGateway telegramGateway
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
        var callbackQueryId = update.getCallbackQuery().getId();
        var tripIdStr = callbackData.substring(callbackPrefix().length());

        try {
            var tripId = Long.parseLong(tripIdStr);
            completeTripUseCase.closeByAdmin(tripId, telegramId.value());
            log.info("Admin {} closed trip {}", telegramId, tripId);

            // ✅ Уведомляем админа через sendMessage
            telegramGateway.sendMessage(
                    telegramId,
                    "✅ Поездка #" + tripId + " закрыта.\nДанные сохранены в историю."
            );

            // ✅ Убираем индикатор загрузки на кнопке
            telegramGateway.answerCallbackQuery(
                    callbackQueryId,
                    "Поездка закрыта",
                    false
            );
        } catch (NumberFormatException e) {
            log.error("Invalid trip ID in callback: {}", tripIdStr, e);
            telegramGateway.answerCallbackQuery(callbackQueryId, "❌ Ошибка данных", true);
        } catch (Exception e) {
            log.error("Failed to close trip {} by admin {}", tripIdStr, telegramId, e);
            telegramGateway.answerCallbackQuery(callbackQueryId, "❌ Произошла ошибка", true);
        }
    }
}