package ru.priyut.coordination.infrastructure.telegram.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.exception.TripNotFoundException;
import ru.priyut.coordination.application.port.in.CancelTripUseCase;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.admin.AdminCommandHandler;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGateway;

/**
 * Обработчик команды /cancel_trip [ID] (сценарий 4.4 ТЗ).
 * Отменяет поездку по ID.
 *
 * Доступно только администраторам.
 */
@Component
public class CancelTripCommandHandler implements AdminCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CancelTripCommandHandler.class);

    private final CancelTripUseCase cancelTripUseCase;
    private final TelegramBotGateway telegramGateway;

    public CancelTripCommandHandler(
            CancelTripUseCase cancelTripUseCase,
            TelegramBotGateway telegramGateway
    ) {
        this.cancelTripUseCase = cancelTripUseCase;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String command() {
        return "/cancel_trip";
    }

    @Override
    public String description() {
        return "Отменить поездку по ID";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        var text = update.getMessage().getText().trim();
        var parts = text.split("\\s+");

        if (parts.length < 2) {
            sendUsageMessage(telegramId);
            return;
        }

        Long tripId;
        try {
            tripId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            sendUsageMessage(telegramId);
            return;
        }

        var reason = parts.length > 2
                ? String.join(" ", java.util.Arrays.copyOfRange(parts, 2, parts.length))
                : "Отменено администратором";

        try {
            cancelTripUseCase.cancelByAdmin(tripId, telegramId.value(), reason);
            log.info("Admin {} cancelled trip {}", telegramId, tripId);

            var msg = new SendMessage();
            msg.setChatId(telegramId.value());
            msg.setText("✅ Поездка #" + tripId + " отменена.");
            telegramGateway.execute(msg);
        } catch (TripNotFoundException e) {
            var msg = new SendMessage();
            msg.setChatId(telegramId.value());
            msg.setText("❌ Поездка #" + tripId + " не найдена.");
            telegramGateway.execute(msg);
        } catch (Exception e) {
            log.error("Failed to cancel trip {} by admin {}", tripId, telegramId, e);
            var msg = new SendMessage();
            msg.setChatId(telegramId.value());
            msg.setText("❌ Ошибка при отмене поездки: " + e.getMessage());
            telegramGateway.execute(msg);
        }
    }

    private void sendUsageMessage(TelegramId telegramId) {
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText("❗ Использование: /cancel_trip [ID] [причина]\n\nПример: /cancel_trip 47 Клиника перенесла приём");
        telegramGateway.execute(msg);
    }
}