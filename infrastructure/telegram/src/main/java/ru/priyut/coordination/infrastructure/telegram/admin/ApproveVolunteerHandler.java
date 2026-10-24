package ru.priyut.coordination.infrastructure.telegram.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.in.ConfirmVolunteerUseCase;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGatewayImpl;

/**
 * Обработчик callback "✅ Подтвердить волонтёра" (сценарий В ТЗ).
 */
@Component
public class ApproveVolunteerHandler implements AdminCallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(ApproveVolunteerHandler.class);

    private final ConfirmVolunteerUseCase confirmVolunteerUseCase;
    private final TelegramBotGatewayImpl telegramGateway;

    public ApproveVolunteerHandler(
            ConfirmVolunteerUseCase confirmVolunteerUseCase,
            TelegramBotGatewayImpl telegramGateway
    ) {
        this.confirmVolunteerUseCase = confirmVolunteerUseCase;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String callbackPrefix() {
        return "volunteer:approve:";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        var callbackData = update.getCallbackQuery().getData();
        var tripIdStr = callbackData.substring(callbackPrefix().length());

        try {
            var tripId = Long.parseLong(tripIdStr);
            confirmVolunteerUseCase.confirm(tripId, telegramId.value());
            log.info("Admin {} approved volunteer for trip {}", telegramId, tripId);

            // Уведомляем админа
            var msg = new SendMessage();
            msg.setChatId(telegramId.value());
            msg.setText("✅ Волонтёр подтверждён на поездку #" + tripId);
            telegramGateway.execute(msg);
        } catch (Exception e) {
            log.error("Failed to approve volunteer for trip {} by admin {}", tripIdStr, telegramId, e);
        }
    }
}