package ru.priyut.coordination.infrastructure.telegram.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.in.ConfirmVolunteerUseCase;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.domain.shared.TelegramId;

@Component
public class RejectTripCallbackHandler implements CallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(RejectTripCallbackHandler.class);

    private final ConfirmVolunteerUseCase useCase;
    private final TelegramGateway telegramGateway; // ✅ Внедряем интерфейс, а не реализацию

    public RejectTripCallbackHandler(ConfirmVolunteerUseCase useCase, TelegramGateway telegramGateway) {
        this.useCase = useCase;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String callbackPrefix() {
        return "trip:reject";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        long tripId = parseTripId(update);
        String callbackQueryId = update.getCallbackQuery().getId();

        try {
            useCase.reject(tripId, telegramId.value());
            log.info("Admin {} rejected volunteer for trip {}", telegramId, tripId);

            // ✅ Уведомляем админа через интерфейс
            telegramGateway.sendMessage(telegramId, "❌ Волонтёр отклонён. Поездка #" + tripId + " снова доступна для откликов.");

            // ✅ Убираем индикатор загрузки на кнопке
            telegramGateway.answerCallbackQuery(callbackQueryId, "Волонтёр отклонён", false);

        } catch (Exception e) {
            log.error("Failed to reject volunteer for trip {} by admin {}", tripId, telegramId, e);
            telegramGateway.answerCallbackQuery(callbackQueryId, "❌ Произошла ошибка", true);
        }
    }

    private long parseTripId(Update update) {
        return Long.parseLong(CallbackDataCodec.decode(update.getCallbackQuery().getData()).payload());
    }

}
