package ru.priyut.coordination.infrastructure.telegram.callback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.dialog.VolunteerRegistrationDialog;

/**
 * Обработка кнопки "Зарегистрироваться" в /start.
 */
@Component
public class RegisterVolunteerCallbackHandler implements CallbackHandler {

    private final VolunteerRegistrationDialog registrationDialog;
    private final TelegramGateway telegramGateway; // ✅ Внедряем интерфейс

    public RegisterVolunteerCallbackHandler(
            VolunteerRegistrationDialog registrationDialog,
            TelegramGateway telegramGateway
    ) {
        this.registrationDialog = registrationDialog;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String callbackPrefix() {
        return "reg:start";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        // Убираем индикатор загрузки
        telegramGateway.answerCallbackQuery(update.getCallbackQuery().getId(), null, false);

        // Запускаем диалог регистрации
        registrationDialog.start(telegramId);
    }
}