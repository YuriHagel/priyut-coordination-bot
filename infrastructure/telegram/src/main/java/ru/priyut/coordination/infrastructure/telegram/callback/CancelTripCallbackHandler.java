package ru.priyut.coordination.infrastructure.telegram.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.volunteer.Volunteer;
import ru.priyut.coordination.infrastructure.telegram.dialog.TripCancelDialog;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGatewayImpl;

/**
 * Обработка кнопки "❌ Отменить" в списке поездок волонтёра.
 * Запускает диалог уточнения причины.
 */
@Component
public class CancelTripCallbackHandler implements CallbackHandler {

    private final TripCancelDialog tripCancelDialog;
    private final VolunteerRepository volunteerRepository;
    private final TelegramBotGatewayImpl telegramGateway;

    public CancelTripCallbackHandler(
            TripCancelDialog tripCancelDialog,
            VolunteerRepository volunteerRepository,
            TelegramBotGatewayImpl telegramGateway
    ) {
        this.tripCancelDialog = tripCancelDialog;
        this.volunteerRepository = volunteerRepository;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String callbackPrefix() {
        return "trip:canc";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        telegramGateway.answerCallbackQuery(update.getCallbackQuery().getId(), null, false);

        try {
            long tripId = Long.parseLong(CallbackDataCodec.decode(update.getCallbackQuery().getData()).payload());

            Volunteer volunteer = volunteerRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> new RuntimeException("Volunteer not found"));

            // Запускаем диалог отмены с предзаполненными данными
            tripCancelDialog.start(telegramId, tripId, volunteer.getId());

        } catch (Exception e) {
            telegramGateway.answerCallbackQuery(update.getCallbackQuery().getId(), "❌ Ошибка при отмене", true);
        }
    }
}