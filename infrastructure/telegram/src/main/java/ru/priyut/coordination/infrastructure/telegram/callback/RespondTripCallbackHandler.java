package ru.priyut.coordination.infrastructure.telegram.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.exception.TripNotFoundException;
import ru.priyut.coordination.application.exception.VolunteerNotFoundException;
import ru.priyut.coordination.application.port.in.RespondToTripUseCase;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.volunteer.Volunteer;

/**
 * Обработка кнопки "🚕 Я поеду".
 */
@Component
public class RespondTripCallbackHandler implements CallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(RespondTripCallbackHandler.class);

    private final RespondToTripUseCase respondToTripUseCase;
    private final VolunteerRepository volunteerRepository;
    private final TelegramGateway telegramGateway; // ✅ Внедряем интерфейс

    public RespondTripCallbackHandler(
            RespondToTripUseCase respondToTripUseCase,
            VolunteerRepository volunteerRepository,
            TelegramGateway telegramGateway
    ) {
        this.respondToTripUseCase = respondToTripUseCase;
        this.volunteerRepository = volunteerRepository;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String callbackPrefix() {
        return "trip:resp";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        var callbackQueryId = update.getCallbackQuery().getId();

        try {
            long tripId = Long.parseLong(CallbackDataCodec.decode(update.getCallbackQuery().getData()).payload());

            // Находим волонтёра по Telegram ID, чтобы получить его внутренний VolunteerId
            Volunteer volunteer = volunteerRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> new VolunteerNotFoundException(telegramId.value()));

            // Вызываем use-case
            respondToTripUseCase.respond(tripId, volunteer.getId());
            log.info("Volunteer {} responded to trip {}", telegramId, tripId);

            telegramGateway.answerCallbackQuery(callbackQueryId, "✅ Заявка отправлена администратору!", false);

        } catch (NumberFormatException e) {
            log.error("Invalid trip ID in callback", e);
            telegramGateway.answerCallbackQuery(callbackQueryId, "❌ Ошибка данных", true);
        } catch (VolunteerNotFoundException e) {
            telegramGateway.answerCallbackQuery(callbackQueryId, "❌ Вы не зарегистрированы", true);
        } catch (TripNotFoundException e) {
            telegramGateway.answerCallbackQuery(callbackQueryId, "❌ Поездка уже закрыта или не найдена", true);
        } catch (Exception e) {
            log.error("Failed to respond to trip", e);
            telegramGateway.answerCallbackQuery(callbackQueryId, "❌ Произошла ошибка", true);
        }
    }
}