package ru.priyut.coordination.infrastructure.telegram.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.priyut.coordination.application.port.outbound.TripRepository;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.domain.trip.TripStatus;
import ru.priyut.coordination.domain.volunteer.Volunteer;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGateway;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Обработчик команды /my_trips (сценарий Д ТЗ).
 * Показывает список активных поездок волонтёра с кнопками действий.
 */
@Component
public class MyTripsCommandHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(MyTripsCommandHandler.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private final VolunteerRepository volunteerRepository;
    private final TripRepository tripRepository;
    private final TelegramBotGateway telegramGateway;

    public MyTripsCommandHandler(
            VolunteerRepository volunteerRepository,
            TripRepository tripRepository,
            TelegramBotGateway telegramGateway
    ) {
        this.volunteerRepository = volunteerRepository;
        this.tripRepository = tripRepository;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String command() {
        return "/my_trips";
    }

    @Override
    public String description() {
        return "Мои активные поездки";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        var volunteerOpt = volunteerRepository.findByTelegramId(telegramId);
        if (volunteerOpt.isEmpty()) {
            sendNotRegisteredMessage(telegramId);
            return;
        }

        var volunteer = volunteerOpt.get();
        if (!volunteer.canRespondToTrips()) {
            sendNotActiveMessage(telegramId, volunteer.getStatus());
            return;
        }

        var trips = tripRepository.findByAssignedVolunteerId(volunteer.getId());
        var activeTrips = trips.stream()
                .filter(t -> t.getStatus().isActive())
                .toList();

        if (activeTrips.isEmpty()) {
            sendNoTripsMessage(telegramId);
            return;
        }

        sendTripsList(telegramId, activeTrips);
    }

    private void sendTripsList(TelegramId telegramId, List<Trip> trips) {
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());

        var sb = new StringBuilder("📋 Ваши активные поездки:\n\n");
        var keyboardRows = new ArrayList<List<InlineKeyboardButton>>();

        for (Trip trip : trips) {
            sb.append(String.format("🚗 #%d — %s (%s)\n",
                    trip.getId().value().getMostSignificantBits(),
                    trip.getRoute().destination(),
                    trip.getTripDateTime().format(DATE_FORMATTER)));
            sb.append("Статус: ").append(statusEmoji(trip.getStatus())).append("\n\n");

            // Кнопки действий
            var row = new ArrayList<InlineKeyboardButton>();

            if (trip.getStatus() == TripStatus.ASSIGNED) {
                var completeBtn = new InlineKeyboardButton();
                completeBtn.setText("🏁 Поездка выполнена");
                completeBtn.setCallbackData("trip:complete:" + trip.getId().value());
                row.add(completeBtn);

                var cancelBtn = new InlineKeyboardButton();
                cancelBtn.setText("❌ Отменить");
                cancelBtn.setCallbackData("trip:cancel:start:" + trip.getId().value());
                row.add(cancelBtn);
            }

            if (!row.isEmpty()) {
                keyboardRows.add(row);
            }
        }

        msg.setText(sb.toString());

        if (!keyboardRows.isEmpty()) {
            var keyboard = new InlineKeyboardMarkup();
            keyboard.setKeyboard(keyboardRows);
            msg.setReplyMarkup(keyboard);
        }

        telegramGateway.execute(msg);
    }

    private String statusEmoji(TripStatus status) {
        return switch (status) {
            case ASSIGNED -> "✅ Подтверждено";
            case PENDING_COMPLETION -> "⏳ На проверке";
            case RESCHEDULED_PENDING -> "🔄 Ожидает подтверждения времени";
            default -> status.name();
        };
    }

    private void sendNotRegisteredMessage(TelegramId telegramId) {
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText("❗ Вы ещё не зарегистрированы.\n\nОтправьте /start для начала работы.");
        telegramGateway.execute(msg);
    }

    private void sendNotActiveMessage(TelegramId telegramId, ru.priyut.coordination.domain.volunteer.VolunteerStatus status) {
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText("❗ Ваш профиль не активен (статус: " + status + ").\n" +
                "Доступ к поездкам временно ограничен.");
        telegramGateway.execute(msg);
    }

    private void sendNoTripsMessage(TelegramId telegramId) {
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText("📋 У вас пока нет активных поездок.\n\n" +
                "Следите за новыми заявками в общем чате волонтёров!");
        telegramGateway.execute(msg);
    }
}