package ru.priyut.coordination.infrastructure.telegram.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.volunteer.VolunteerStatus;
import ru.priyut.coordination.infrastructure.telegram.dialog.VolunteerRegistrationDialog;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGateway;

import java.io.File;
import java.util.List;

/**
 * Обработчик команды /start (сценарий 4.1 ТЗ).
 *
 * Логика:
 * - Если пользователь не зарегистрирован → отправляет PDF политики + кнопку "Зарегистрироваться"
 * - Если анкета на модерации → показывает статус
 * - Если активен → показывает главное меню
 */
@Component
public class StartCommandHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(StartCommandHandler.class);

    private final VolunteerRepository volunteerRepository;
    private final VolunteerRegistrationDialog registrationDialog;
    private final TelegramBotGateway telegramGateway;
    private final String policyVersion;
    private final String policyDocumentPath;

    public StartCommandHandler(
            VolunteerRepository volunteerRepository,
            VolunteerRegistrationDialog registrationDialog,
            TelegramBotGateway telegramGateway,
            @Value("${priyut.pdn.policy-version:1.0}") String policyVersion,
            @Value("${priyut.pdn.policy-document-path:Politika_PDN_Priyut.pdf}") String policyDocumentPath
    ) {
        this.volunteerRepository = volunteerRepository;
        this.registrationDialog = registrationDialog;
        this.telegramGateway = telegramGateway;
        this.policyVersion = policyVersion;
        this.policyDocumentPath = policyDocumentPath;
    }

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "Начать работу с ботом";
    }

    @Override
    public void handle(Update update, TelegramId telegramId) {
        var volunteerOpt = volunteerRepository.findByTelegramId(telegramId);

        if (volunteerOpt.isEmpty()) {
            // Новый пользователь — запускаем регистрацию
            handleNewUser(telegramId);
            return;
        }

        var volunteer = volunteerOpt.get();
        switch (volunteer.getStatus()) {
            case PENDING_MODERATION -> sendPendingModerationMessage(telegramId);
            case ACTIVE, TEMPORARILY_UNAVAILABLE -> sendActiveUserMenu(telegramId);
            case REJECTED -> sendRejectedMessage(telegramId);
            case DELETION_REQUESTED, DELETED, ARCHIVED -> sendDeletedMessage(telegramId);
        }
    }

    /**
     * Новый пользователь — отправляем PDF и кнопку регистрации.
     */
    private void handleNewUser(TelegramId telegramId) {
        // 1. Отправляем PDF политики
        try {
            var pdfResource = new ClassPathResource("docs/" + policyDocumentPath);
            var document = new SendDocument();
            document.setChatId(telegramId.value().toString());
            document.setDocument(new org.telegram.telegrambots.meta.api.objects.InputFile(
                    pdfResource.getInputStream(), policyDocumentPath));
            document.setCaption("📄 Политика обработки персональных данных");
            telegramGateway.execute(document);
        } catch (Exception e) {
            log.error("Failed to send policy PDF to user {}", telegramId, e);
        }

        // 2. Отправляем текст согласия
        var consentMessage = new SendMessage();
        consentMessage.setChatId(telegramId.value());
        consentMessage.setText(
                "🤖 Здравствуйте! Я бот помощи приюту «Приютино».\n\n" +
                        "Я помогу вам стать автоволонтёром.\n\n" +
                        "📄 Пожалуйста, ознакомьтесь с документом выше.\n\n" +
                        "Нажимая кнопку «Зарегистрироваться», вы даёте согласие " +
                        "на обработку персональных данных (версия " + policyVersion + ")."
        );

        // 3. Кнопка "Зарегистрироваться"
        var registerButton = new InlineKeyboardButton();
        registerButton.setText("✅ Зарегистрироваться");
        registerButton.setCallbackData("register:start");

        var howItWorksButton = new InlineKeyboardButton();
        howItWorksButton.setText("❓ Как это работает");
        howItWorksButton.setCallbackData("register:how_it_works");

        var keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(
                List.of(registerButton),
                List.of(howItWorksButton)
        ));
        consentMessage.setReplyMarkup(keyboard);

        telegramGateway.execute(consentMessage);
    }

    private void sendPendingModerationMessage(TelegramId telegramId) {
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText(
                "⏳ Ваша анкета находится на модерации.\n\n" +
                        "Ориентировочное время проверки: до 24 часов.\n\n" +
                        "После одобрения вы получите доступ к заявкам."
        );
        telegramGateway.execute(msg);
    }

    private void sendActiveUserMenu(TelegramId telegramId) {
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText(
                "👋 С возвращением!\n\n" +
                        "Доступные команды:\n" +
                        "/my_trips — мои поездки\n" +
                        "/profile — мой профиль\n" +
                        "/delete_profile — удалить анкету\n" +
                        "/withdraw_consent — отозвать согласие на ПДн"
        );
        telegramGateway.execute(msg);
    }

    private void sendRejectedMessage(TelegramId telegramId) {
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText("❌ К сожалению, ваша анкета была отклонена.\n\n" +
                "Если вы считаете, что это ошибка, свяжитесь с администратором.");
        telegramGateway.execute(msg);
    }

    private void sendDeletedMessage(TelegramId telegramId) {
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText("ℹ️ Ваша анкета удалена.\n\n" +
                "Если хотите вернуться — просто заполните анкету заново.");
        telegramGateway.execute(msg);
    }
}