package ru.priyut.coordination.infrastructure.telegram.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.in.ConfirmVolunteerUseCase;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGateway;

/**
 * Обработчик callback "❌ Отклонить волонтёра" (сценарий В ТЗ).
 */
@Component
public class RejectVolunteerHandler implements AdminCallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(RejectVolunteerHandler.class);

    private final ConfirmVolunteerUseCase confirmVolunteerUseCase;
    private final TelegramBotGateway telegramGateway;

    public RejectVolunteerHandler(
            ConfirmVolunteerUseCase confirmVolunteerUseCase,
            TelegramBotGateway telegramGateway
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

            var msg = new SendMessage();
            msg.setChatId(telegramId.value());
            msg.setText("❌ Волонтёр отклонён. Поездка #" + tripId + " снова доступна для откликов.");
            telegramGateway.execute(msg);
        } catch (Exception e) {
            log.error("Failed to reject volunteer for trip {} by admin {}", tripIdStr, telegramId, e);
        }
    }
}