package ru.priyut.coordination.infrastructure.telegram.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGateway;

import java.util.List;

/**
 * Обработчик команды /profile (F5 ТЗ).
 * Показывает данные волонтёра и кнопки редактирования.
 */
@Component
public class ProfileCommandHandler implements CommandHandler {

    private final VolunteerRepository volunteerRepository;
    private final TelegramBotGateway telegramGateway;

    public ProfileCommandHandler(
            VolunteerRepository volunteerRepository,
            TelegramBotGateway telegramGateway
    ) {
        this.volunteerRepository = volunteerRepository;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String command() {
        return "/profile";
    }

    @Override
    public String description() {
        return "Мой профиль";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        var volunteerOpt = volunteerRepository.findByTelegramId(telegramId);
        if (volunteerOpt.isEmpty()) {
            var msg = new SendMessage();
            msg.setChatId(telegramId.value());
            msg.setText("❗ Вы ещё не зарегистрированы.\n\nОтправьте /start для начала работы.");
            telegramGateway.execute(msg);
            return;
        }

        var volunteer = volunteerOpt.get();

        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText(
                "📌 Ваш профиль:\n\n" +
                        "👤 " + volunteer.getFullName() + "\n" +
                        "📞 " + volunteer.getPhone().value() + "\n" +
                        "📍 " + volunteer.getDistrict().name() + "\n" +
                        "🚗 " + volunteer.getCar().formatted() + "\n" +
                        "⏰ " + volunteer.getAvailability().formatted() + "\n" +
                        "📊 Статус: " + volunteer.getStatus()
        );

        // Кнопки действий
        var toggleBtn = new InlineKeyboardButton();
        toggleBtn.setText(volunteer.getAvailability().status() ==
                ru.priyut.coordination.domain.volunteer.Availability.AvailabilityStatus.AVAILABLE
                ? "⏸ Временно недоступен"
                : "✅ Доступен");
        toggleBtn.setCallbackData("profile:toggle_availability");

        var keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(List.of(toggleBtn)));
        msg.setReplyMarkup(keyboard);

        telegramGateway.execute(msg);
    }
}