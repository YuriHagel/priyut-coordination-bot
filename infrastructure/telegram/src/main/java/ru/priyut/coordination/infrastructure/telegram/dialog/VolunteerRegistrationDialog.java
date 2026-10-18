package ru.priyut.coordination.infrastructure.telegram.dialog;

import org.springframework.stereotype.Component;
import ru.priyut.coordination.application.command.RegisterVolunteerCommand;
import ru.priyut.coordination.application.port.in.RegisterVolunteerUseCase;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.shared.UtcClock;
import ru.priyut.coordination.domain.volunteer.*;
import ru.priyut.coordination.infrastructure.telegram.dialog.steps.*;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGateway;

import java.util.List;

/**
 * Диалог регистрации волонтёра (сценарий 4.1 ТЗ).
 * Шаги:
 * 1. Согласие на ПДн (отправка PDF + кнопка)
 * 2. Имя
 * 3. Телефон
 * 4. Район
 * 5. Автомобиль
 * 6. Дни доступности
 * 7. Время доступности
 * 8. Подтверждение (сводка)
 */
@Component
public class VolunteerRegistrationDialog extends Dialog {

    public static final String CONSENT_VERSION = "1.0";
    public static final String CONSENT_DOCUMENT = "Politika_PDN_Priyut.pdf";

    private final RegisterVolunteerUseCase registerVolunteerUseCase;
    private final List<DialogStep> steps;

    public VolunteerRegistrationDialog(
            ConversationStore conversationStore,
            TelegramBotGateway telegramGateway,
            UtcClock clock,
            RegisterVolunteerUseCase registerVolunteerUseCase
    ) {
        super(conversationStore, telegramGateway, clock);
        this.registerVolunteerUseCase = registerVolunteerUseCase;

        this.steps = List.of(
                new ConsentStep(CONSENT_VERSION, CONSENT_DOCUMENT),
                new NameStep(),
                new PhoneStep(),
                new DistrictStep(),
                new CarStep(),
                new AvailabilityDaysStep(),
                new AvailabilityTimeStep(),
                new ConfirmationStep()
        );
    }

    @Override
    protected ConversationContext.DialogType dialogType() {
        return ConversationContext.DialogType.VOLUNTEER_REGISTRATION;
    }

    @Override
    protected List<DialogStep> steps() {
        return steps;
    }

    @Override
    protected void onComplete(TelegramId telegramId, ConversationContext context) {
        // Собираем команду из контекста
        var command = new RegisterVolunteerCommand(
                telegramId,
                context.require("fullName", String.class),
                PhoneNumber.of(context.require("phone", String.class)),
                District.of(context.require("district", String.class)),
                parseCar(context),
                parseAvailability(context),
                CONSENT_VERSION
        );

        // Регистрируем волонтёра через use-case
        try {
            registerVolunteerUseCase.register(command);
            log.info("Volunteer registered successfully: {}", telegramId);
        } catch (Exception e) {
            log.error("Failed to register volunteer {}", telegramId, e);
            var errorMsg = new org.telegram.telegrambots.meta.api.methods.send.SendMessage();
            errorMsg.setChatId(telegramId.value());
            errorMsg.setText("❌ Произошла ошибка при регистрации. Попробуйте позже или обратитесь к администратору.");
            sendMessage(telegramId, errorMsg);
        }
    }

    private Car parseCar(ConversationContext context) {
        var carString = context.require("car", String.class);
        // Формат: "Kia Rio, серый" или "Kia Rio"
        var parts = carString.split(",", 2);
        var brandModel = parts[0].trim().split("\\s+", 2);
        var brand = brandModel[0];
        var model = brandModel.length > 1 ? brandModel[1] : "";
        var color = parts.length > 1 ? parts[1].trim() : "";
        return Car.of(brand, model, color);
    }

    private Availability parseAvailability(ConversationContext context) {
        var days = context.require("availabilityDays", java.util.Set.class);
        var timeFrom = context.require("timeFrom", java.time.LocalTime.class);
        var timeTo = context.require("timeTo", java.time.LocalTime.class);
        return Availability.of(
                days,
                timeFrom,
                timeTo,
                Availability.AvailabilityStatus.AVAILABLE
        );
    }
}