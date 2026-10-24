package ru.priyut.coordination.infrastructure.telegram.dialog;

import org.springframework.stereotype.Component;
import ru.priyut.coordination.application.command.CreateTripCommand;
import ru.priyut.coordination.application.port.in.CreateTripUseCase;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.shared.UtcClock;
import ru.priyut.coordination.domain.trip.Route;
import ru.priyut.coordination.domain.trip.TripComment;
import ru.priyut.coordination.domain.trip.TripType;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGatewayImpl;

import java.util.List;

/**
 * Диалог создания поездки администратором (сценарий 4.2 ТЗ).
 * Шаги:
 * 1. Куда везти (клиника + адрес)
 * 2. Дата и время
 * 3. Кого везём и зачем
 * 4. Тип поездки (обычная/срочная)
 * 5. Комментарий (опционально)
 * 6. Подтверждение
 */
@Component
public class TripCreationDialog extends Dialog {

    private final CreateTripUseCase createTripUseCase;
    private final List<DialogStep> steps;

    public TripCreationDialog(
            ConversationStore conversationStore,
            TelegramBotGatewayImpl telegramGateway,
            UtcClock clock,
            CreateTripUseCase createTripUseCase
    ) {
        super(conversationStore, telegramGateway, clock);
        this.createTripUseCase = createTripUseCase;

        this.steps = List.of(
                new TripDestinationStep(),
                new TripDateTimeStep(),
                new TripTaskStep(),
                new TripTypeStep(),
                new TripCommentStep(),
                new TripConfirmationStep()
        );
    }

    @Override
    protected ConversationContext.DialogType dialogType() {
        return ConversationContext.DialogType.TRIP_CREATION;
    }

    @Override
    protected List<DialogStep> steps() {
        return steps;
    }

    @Override
    protected void onComplete(TelegramId telegramId, ConversationContext context) {
        var command = new CreateTripCommand(
                telegramId.value(),
                Route.of(
                        "Приют",
                        context.require("destination", String.class),
                        context.require("destination", String.class)
                ),
                context.require("tripDateTime", java.time.LocalDateTime.class),
                context.require("taskDescription", String.class),
                context.require("tripType", TripType.class),
                TripComment.of(context.get("comment", String.class).orElse(""))
        );

        try {
            var trip = createTripUseCase.createDraft(command);
            log.info("Trip created: {} by admin {}", trip.getId(), telegramId);
            // TODO: Показать превью с кнопками "Опубликовать" / "Изменить" / "Отмена"
        } catch (Exception e) {
            log.error("Failed to create trip by admin {}", telegramId, e);
        }
    }
}