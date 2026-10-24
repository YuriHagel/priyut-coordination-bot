package ru.priyut.coordination.infrastructure.telegram.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.in.ConfirmVolunteerUseCase;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGatewayImpl;

/**
 * Обработка кнопок "✅ Подтвердить" и "❌ Отклонить" администратором.
 */
@Component
public class ConfirmTripCallbackHandler implements CallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(ConfirmTripCallbackHandler.class);

    private final ConfirmVolunteerUseCase confirmVolunteerUseCase;
    private final TelegramBotGatewayImpl telegramGateway;

    public ConfirmTripCallbackHandler(
            ConfirmVolunteerUseCase confirmVolunteerUseCase,
            TelegramBotGatewayImpl telegramGateway
    ) {
        this.confirmVolunteerUseCase = confirmVolunteerUseCase;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String callbackPrefix() {
        // Этот хендлер обрабатывает два префикса, но в интерфейсе мы возвращаем базовый.
        // Router должен быть немного умнее, или мы регистрируем этот класс дважды с разными бинами.
        // Для простоты, сделаем два отдельных класса или один с проверкой в handle.
        // Лучше сделаем два отдельных компонента для чистоты.
        return "trip:approve";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        // Реализация вынесена в ApproveTripCallbackHandler и RejectTripCallbackHandler ниже для чистоты
    }
}

// --- Разделяем на два чистых компонента ---

@Component
class ApproveTripCallbackHandler implements CallbackHandler {
    private final ConfirmVolunteerUseCase useCase;
    private final TelegramBotGatewayImpl gateway;

    public ApproveTripCallbackHandler(ConfirmVolunteerUseCase useCase, TelegramBotGatewayImpl gateway) {
        this.useCase = useCase;
        this.gateway = gateway;
    }

    @Override
    public String callbackPrefix() { return "trip:approve"; }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        long tripId = parseTripId(update);
        useCase.confirm(tripId, telegramId.value());
        gateway.answerCallbackQuery(update.getCallbackQuery().getId(), "✅ Волонтёр подтверждён", false);
    }

    private long parseTripId(Update update) {
        return Long.parseLong(CallbackDataCodec.decode(update.getCallbackQuery().getData()).payload());
    }
}

@Component
class RejectTripCallbackHandler implements CallbackHandler {
    private final ConfirmVolunteerUseCase useCase;
    private final TelegramBotGatewayImpl gateway;

    public RejectTripCallbackHandler(ConfirmVolunteerUseCase useCase, TelegramBotGatewayImpl gateway) {
        this.useCase = useCase;
        this.gateway = gateway;
    }

    @Override
    public String callbackPrefix() { return "trip:reject"; }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        long tripId = parseTripId(update);
        useCase.reject(tripId, telegramId.value());
        gateway.answerCallbackQuery(update.getCallbackQuery().getId(), "❌ Волонтёр отклонён", false);
    }

    private long parseTripId(Update update) {
        return Long.parseLong(CallbackDataCodec.decode(update.getCallbackQuery().getData()).payload());
    }
}