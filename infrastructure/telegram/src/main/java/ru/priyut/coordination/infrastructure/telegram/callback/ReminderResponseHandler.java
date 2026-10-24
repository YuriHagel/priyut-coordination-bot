package ru.priyut.coordination.infrastructure.telegram.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.in.CancelTripUseCase;
import ru.priyut.coordination.application.port.outbound.TripRepository;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.volunteer.Volunteer;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGatewayImpl;

public class ReminderResponseHandler implements CallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(ReminderResponseHandler.class);

    private final CancelTripUseCase cancelTripUseCase;
    private final VolunteerRepository volunteerRepository;
    private final TripRepository tripRepository;
    private final TelegramBotGatewayImpl telegramGateway;

    public ReminderResponseHandler(
            CancelTripUseCase cancelTripUseCase,
            VolunteerRepository volunteerRepository,
            TripRepository tripRepository,
            TelegramBotGatewayImpl telegramGateway
    ) {
        this.cancelTripUseCase = cancelTripUseCase;
        this.volunteerRepository = volunteerRepository;
        this.tripRepository = tripRepository;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String callbackPrefix() {
        return "rem"; // Обрабатывает rem:confirm и rem:decline
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        var callbackQuery = update.getCallbackQuery();
        var decoded = CallbackDataCodec.decode(callbackQuery.getData());
        var tripId = Long.parseLong(decoded.payload());
        var callbackQueryId = callbackQuery.getId();

        try {
            Volunteer volunteer = volunteerRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> new RuntimeException("Volunteer not found"));

            if ("confirm".equals(decoded.action().replace("rem:", ""))) {
                // Просто подтверждаем, можно обновить флаг в БД (опционально)
                log.info("Volunteer {} confirmed reminder for trip {}", telegramId, tripId);
                telegramGateway.answerCallbackQuery(callbackQueryId, "✅ Спасибо! Администратор уведомлён.", false);

                // Отправляем сообщение в чат (опционально, через gateway)
                // telegramGateway.sendMessage(telegramId, "✅ Спасибо! Ждём вас завтра.");

            }else if ("decline".equals(decoded.action().replace("rem:", ""))) {
                // Инициируем отмену
                var command = new ru.priyut.coordination.application.command.CancelTripCommand(
                        tripId,
                        volunteer.getId(),
                        "Отказался по напоминанию"
                );
                cancelTripUseCase.cancelByVolunteer(command);
                log.info("Volunteer {} declined reminder and cancelled trip {}", telegramId, tripId);

                telegramGateway.answerCallbackQuery(callbackQueryId, "❌ Поездка отменена. Мы найдём другого волонтёра.", false);
                telegramGateway.sendMessage(telegramId, "❌ Поездка отменена. Спасибо, что предупредили заранее!");
            }

        } catch (Exception e) {
            log.error("Failed to process reminder response for trip {}", tripId, e);
            telegramGateway.answerCallbackQuery(callbackQueryId, "❌ Произошла ошибка", true);
        }
    }
}
