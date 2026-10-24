package ru.priyut.coordination.infrastructure.telegram.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.priyut.coordination.application.port.in.DeleteProfileUseCase;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGatewayImpl;

import java.util.List;

/**
 * Обработчик команды /delete_profile (сценарий Ж, F7 ТЗ).
 * Запускает процесс удаления персональных данных (152-ФЗ).
 */
@Component
public class DeleteProfileCommandHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(DeleteProfileCommandHandler.class);

    private final DeleteProfileUseCase deleteProfileUseCase;
    private final TelegramBotGatewayImpl telegramGateway;

    public DeleteProfileCommandHandler(
            DeleteProfileUseCase deleteProfileUseCase,
            TelegramBotGatewayImpl telegramGateway
    ) {
        this.deleteProfileUseCase = deleteProfileUseCase;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public String command() {
        return "/delete_profile";
    }

    @Override
    public String description() {
        return "Удалить анкету волонтёра";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText(
                "⚠ Вы собираетесь удалить свою анкету из базы волонтёров.\n\n" +
                        "Это действие необратимо. Ваши данные будут полностью удалены:\n" +
                        "• Имя\n" +
                        "• Телефон\n" +
                        "• Район\n" +
                        "• Автомобиль\n\n" +
                        "Срок удаления: в течение 3 дней.\n" +
                        "Резервные копии будут очищены через 7 дней.\n\n" +
                        "Вы уверены?"
        );

        var noBtn = new InlineKeyboardButton();
        noBtn.setText("❌ Нет, отменить");
        noBtn.setCallbackData("delete_profile:cancel");

        var yesBtn = new InlineKeyboardButton();
        yesBtn.setText("✅ Да, удалить");
        yesBtn.setCallbackData("delete_profile:confirm");

        var keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(List.of(noBtn, yesBtn)));
        msg.setReplyMarkup(keyboard);

        telegramGateway.execute(msg);
    }
}