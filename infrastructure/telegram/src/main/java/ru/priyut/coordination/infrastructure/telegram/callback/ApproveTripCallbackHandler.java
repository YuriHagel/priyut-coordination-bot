package ru.priyut.coordination.infrastructure.telegram.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.in.ConfirmVolunteerUseCase;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.domain.shared.TelegramId;

/**
 * Обработчик callback "✅ Подтвердить волонтёра" (сценарий В ТЗ).
 */
@Component
public class ApproveTripCallbackHandler implements CallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(ApproveTripCallbackHandler.class);

    private final ConfirmVolunteerUseCase useCase;
    private final TelegramGateway telegramGateway; // ✅ Внедряем интерфейс, а не реализацию

    public ApproveTripCallbackHandler(ConfirmVolunteerUseCase useCase, TelegramGateway telegramGateway) {
        this.useCase = useCase;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String callbackPrefix() {
        return "trip:approve";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        long tripId = parseTripId(update);
        String callbackQueryId = update.getCallbackQuery().getId();

        try {
            useCase.confirm(tripId, telegramId.value());
            log.info("Admin {} approved volunteer for trip {}", telegramId, tripId);

            // ✅ Уведомляем админа через интерфейс
            telegramGateway.sendMessage(telegramId, "✅ Волонтёр подтверждён на поездку #" + tripId);

            // ✅ Убираем индикатор загрузки на кнопке
            telegramGateway.answerCallbackQuery(callbackQueryId, "Волонтёр подтверждён", false);

        } catch (Exception e) {
            log.error("Failed to approve volunteer for trip {} by admin {}", tripId, telegramId, e);
            telegramGateway.answerCallbackQuery(callbackQueryId, "❌ Произошла ошибка", true);
        }
    }

    private long parseTripId(Update update) {
        return Long.parseLong(CallbackDataCodec.decode(update.getCallbackQuery().getData()).payload());
    }
}
