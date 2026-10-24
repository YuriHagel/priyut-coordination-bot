package ru.priyut.coordination.infrastructure.telegram.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.admin.AdminCommandHandler;
import ru.priyut.coordination.infrastructure.telegram.dialog.TripCreationDialog;

/**
 * Обработчик команды /new_trip (сценарий 4.2 ТЗ).
 * Запускает диалог создания поездки.
 *
 * Доступно только администраторам (маркер AdminCommandHandler).
 */
@Component
public class NewTripCommandHandler implements AdminCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(NewTripCommandHandler.class);

    private final TripCreationDialog tripCreationDialog;

    public NewTripCommandHandler(TripCreationDialog tripCreationDialog) {
        this.tripCreationDialog = tripCreationDialog;
    }

    @Override
    public String command() {
        return "/new_trip";
    }

    @Override
    public String description() {
        return "Создать новую поездку";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        log.info("Admin {} started trip creation dialog", telegramId);
        tripCreationDialog.start(telegramId);
    }
}