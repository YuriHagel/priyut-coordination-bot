package ru.priyut.coordination.infrastructure.telegram.notification;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.infrastructure.telegram.keyboard.InlineKeyboardFactory;

import java.util.List;

/**
 * Сервис высокого уровня для бизнес-сценариев рассылки.
 */
@Service
public class BroadcastService {

    private final BatchMessageDispatcher batchDispatcher;

    public BroadcastService(BatchMessageDispatcher batchDispatcher) {
        this.batchDispatcher = batchDispatcher;
    }

    /**
     * Рассылает информацию о новой поездке всем активным волонтёрам (Сценарий 4.2, 4.3).
     */
    public void broadcastNewTrip(Trip trip, List<TelegramId> volunteerIds) {
        if (volunteerIds.isEmpty()) {
            return;
        }

        // Формируем текст сообщения (в реальном проекте лучше вынести в TripMessageRenderer)
        String urgencyPrefix = trip.isUrgent() ? "🚨 СРОЧНАЯ заявка\n" : "🆕 Новая заявка\n";
        String text = String.format(
                "%s📋 ID: #%d\n📍 Маршрут: %s\n🕐 Время: %s\n🐾 Задача: %s",
                urgencyPrefix,
                trip.getId().value().getMostSignificantBits(), // Упрощенный ID для отображения
                trip.getRoute().formatted(),
                trip.getTripDateTime(),
                trip.getTaskDescription()
        );

        // Создаем сообщение с помощью Builder (как мы выяснили ранее)
        SendMessage message = SendMessage.builder()
                .text(text)
                .replyMarkup(InlineKeyboardFactory.createTripResponseKeyboard(
                        trip.getId().value().getMostSignificantBits()
                ))
                .build();

        // Запускаем асинхронную рассылку
        batchDispatcher.dispatchBatch(volunteerIds, message);
    }
}