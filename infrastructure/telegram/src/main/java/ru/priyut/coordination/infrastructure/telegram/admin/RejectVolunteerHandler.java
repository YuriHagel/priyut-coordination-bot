package ru.priyut.coordination.infrastructure.telegram.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.in.ConfirmVolunteerUseCase;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.domain.shared.TelegramId;

/**
 * Обработчик callback "❌ Отклонить волонтёра" (сценарий В ТЗ).
 */
@Component
public class RejectVolunteerHandler implements AdminCallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(RejectVolunteerHandler.class);

    private final ConfirmVolunteerUseCase confirmVolunteerUseCase;
    private final TelegramGateway telegramGateway; // ✅ Внедряем интерфейс, а не реализацию

    public RejectVolunteerHandler(
            ConfirmVolunteerUseCase confirmVolunteerUseCase,
            TelegramGateway telegramGateway
    ) {
        this.confirmVolunteerUseCase = confirmVolunteerUseCase;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String callbackPrefix() {
        return "volunteer:reject:";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        var callbackData = update.getCallbackQuery().getData();
        var tripIdStr = callbackData.substring(callbackPrefix().length());

        try {
            var tripId = Long.parseLong(tripIdStr);
            confirmVolunteerUseCase.reject(tripId, telegramId.value());
            log.info("Admin {} rejected volunteer for trip {}", telegramId, tripId);

            // ✅ Используем метод sendMessage из интерфейса TelegramGateway
            telegramGateway.sendMessage(
                    telegramId,
                    "❌ Волонтёр отклонён. Поездка #" + tripId + " снова доступна для откликов."
            );

            // ✅ Убираем индикатор загрузки на кнопке
            telegramGateway.answerCallbackQuery(
                    update.getCallbackQuery().getId(),
                    "Волонтёр отклонён",
                    false
            );

        } catch (NumberFormatException e) {
            log.error("Invalid trip ID in callback: {}", tripIdStr, e);
            telegramGateway.answerCallbackQuery(
                    update.getCallbackQuery().getId(),
                    "❌ Ошибка данных",
                    true
            );
        } catch (Exception e) {
            log.error("Failed to reject volunteer for trip {} by admin {}", tripIdStr, telegramId, e);
            telegramGateway.answerCallbackQuery(
                    update.getCallbackQuery().getId(),
                    "❌ Произошла ошибка",
                    true
            );
        }
    }
}