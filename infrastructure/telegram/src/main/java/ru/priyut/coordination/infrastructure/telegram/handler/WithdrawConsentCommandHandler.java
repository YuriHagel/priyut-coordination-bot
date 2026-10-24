package ru.priyut.coordination.infrastructure.telegram.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGateway;

import java.util.List;

/**
 * Обработчик команды /withdraw_consent (сценарий Ж, F14 ТЗ).
 * Запускает процесс отзыва согласия на обработку ПДн.
 */
@Component
public class WithdrawConsentCommandHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(WithdrawConsentCommandHandler.class);

    private final TelegramBotGateway telegramGateway;
    private final String policyVersion;

    public WithdrawConsentCommandHandler(
            TelegramBotGateway telegramGateway,
            @Value("${priyut.pdn.policy-version:1.0}") String policyVersion
    ) {
        this.telegramGateway = telegramGateway;
        this.policyVersion = policyVersion;
    }

    @Override
    public String command() {
        return "/withdraw_consent";
    }

    @Override
    public String description() {
        return "Отозвать согласие на обработку ПДн";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText(
                "📄 Отзыв согласия на обработку персональных данных\n\n" +
                        "Вы подтверждаете, что хотите отозвать согласие\n" +
                        "на обработку ПДн (версия " + policyVersion + ")?\n\n" +
                        "Это действие будет зафиксировано в журнале 152-ФЗ."
        );

        var noBtn = new InlineKeyboardButton();
        noBtn.setText("❌ Нет");
        noBtn.setCallbackData("withdraw_consent:cancel");

        var yesBtn = new InlineKeyboardButton();
        yesBtn.setText("✅ Да, отозвать");
        yesBtn.setCallbackData("withdraw_consent:confirm");

        var keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(List.of(noBtn, yesBtn)));
        msg.setReplyMarkup(keyboard);

        telegramGateway.execute(msg);
    }
}