package ru.priyut.coordination.infrastructure.telegram.dialog;

import org.springframework.stereotype.Component;
import ru.priyut.coordination.application.command.CancelTripCommand;
import ru.priyut.coordination.application.port.in.CancelTripUseCase;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.shared.UtcClock;
import ru.priyut.coordination.domain.volunteer.VolunteerId;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGatewayImpl;

import java.util.List;

/**
 * Диалог отмены поездки волонтёром (сценарий 4.4 ТЗ).
 * Шаги:
 * 1. Выбор причины отмены
 * 2. Подтверждение
 */
@Component
public class TripCancelDialog extends Dialog {

    private final CancelTripUseCase cancelTripUseCase;
    private final List<DialogStep> steps;

    public TripCancelDialog(
            ConversationStore conversationStore,
            TelegramBotGatewayImpl telegramGateway,
            UtcClock clock,
            CancelTripUseCase cancelTripUseCase
    ) {
        super(conversationStore, telegramGateway, clock);
        this.cancelTripUseCase = cancelTripUseCase;

        this.steps = List.of(
                new CancelReasonStep(),
                new CancelConfirmationStep()
        );
    }

    @Override
    protected ConversationContext.DialogType dialogType() {
        return ConversationContext.DialogType.TRIP_CANCEL;
    }

    @Override
    protected List<DialogStep> steps() {
        return steps;
    }

    /**
     * Запуск диалога отмены с параметрами (tripId и volunteerId).
     */
    public void start(TelegramId telegramId, Long tripId, VolunteerId volunteerId) {
        // Создаём контекст с предзаполненными данными
        var context = ConversationContext.start(telegramId, dialogType(), clock.now());
        context.put("tripId", tripId);
        context.put("volunteerId", volunteerId);
        conversationStore.save(context);

        // Рендерим первый шаг
        var firstStep = steps().get(0);
        var message = firstStep.render(context);
        sendMessage(telegramId, message);
    }

    @Override
    protected void onComplete(TelegramId telegramId, ConversationContext context) {
        var tripId = context.require("tripId", Long.class);
        var volunteerId = context.require("volunteerId", VolunteerId.class);
        var reason = context.require("cancelReason", String.class);

        var command = new CancelTripCommand(tripId, volunteerId, reason);

        try {
            cancelTripUseCase.cancelByVolunteer(command);
            log.info("Trip {} cancelled by volunteer {}", tripId, telegramId);
        } catch (Exception e) {
            log.error("Failed to cancel trip {} by volunteer {}", tripId, telegramId, e);
        }
    }
}