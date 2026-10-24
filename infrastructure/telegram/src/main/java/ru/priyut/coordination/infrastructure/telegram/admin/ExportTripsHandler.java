package ru.priyut.coordination.infrastructure.telegram.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.outbound.TripRepository;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGatewayImpl;

import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Обработчик команды /export_trips (F4 ТЗ).
 * Экспортирует историю поездок в CSV и отправляет админу.
 */
@Component
public class ExportTripsHandler implements AdminCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(ExportTripsHandler.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TripRepository tripRepository;
    private final TelegramBotGatewayImpl telegramGateway;

    public ExportTripsHandler(
            TripRepository tripRepository,
            TelegramBotGatewayImpl telegramGateway
    ) {
        this.tripRepository = tripRepository;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String command() {
        return "/export_trips";
    }

    @Override
    public String description() {
        return "Экспорт истории поездок в CSV";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        log.info("Admin {} requested trips export", telegramId);

        // Получаем все завершённые поездки
        var trips = tripRepository.findByStatus(ru.priyut.coordination.domain.trip.TripStatus.COMPLETED);

        if (trips.isEmpty()) {
            var msg = new SendMessage();
            msg.setChatId(telegramId.value());
            msg.setText("ℹ️ Нет завершённых поездок для экспорта.");
            telegramGateway.execute(msg);
            return;
        }

        // Генерируем CSV
        var csv = generateCsv(trips);

        // Отправляем как сообщение (для MVP; в production — как документ)
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText("📊 Экспорт поездок (" + trips.size() + " записей):\n\n" +
                "```\n" + csv + "\n```");
        msg.setParseMode("Markdown");
        telegramGateway.execute(msg);
    }

    private String generateCsv(List<Trip> trips) {
        var writer = new StringWriter();
        writer.write("id,date_time,route,task,type,status,volunteer_id\n");

        for (Trip trip : trips) {
            writer.write(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                    trip.getId().value().getMostSignificantBits(),
                    trip.getTripDateTime().format(DATE_FORMATTER),
                    escapeCsv(trip.getRoute().formatted()),
                    escapeCsv(trip.getTaskDescription()),
                    trip.getType(),
                    trip.getStatus(),
                    trip.getAssignedVolunteerId() != null
                            ? trip.getAssignedVolunteerId().value().toString()
                            : ""
            ));
        }

        return writer.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}