package ru.priyut.coordination.infrastructure.telegram.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.keyboard.InlineKeyboardFactory;

/**
 * Специализированный сервис для отправки Политики обработки ПДн (сценарий 4.1 ТЗ).
 * Инкапсулирует путь к файлу и текст согласия, чтобы не хардкодить их в хендлерах.
 */
@Component
public class PdfPolicySender {

    private final DocumentSender documentSender;
    private final String policyClasspath;
    private final String policyFileName;
    private final String policyVersion;

    public PdfPolicySender(
            DocumentSender documentSender,
            @Value("${priyut.pdn.policy-classpath:docs/Politika_PDN_Priyut.pdf}") String policyClasspath,
            @Value("${priyut.pdn.policy-filename:Politika_PDN_Priyut.pdf}") String policyFileName,
            @Value("${priyut.pdn.policy-version:1.0}") String policyVersion
    ) {
        this.documentSender = documentSender;
        this.policyClasspath = policyClasspath;
        this.policyFileName = policyFileName;
        this.policyVersion = policyVersion;
    }

    /**
     * Отправляет PDF политики и клавиатуру для начала регистрации.
     */
    public void sendPolicyAndStartButton(TelegramId telegramId) {
        String caption = String.format(
                "🤖 Здравствуйте! Я бот помощи приюту «Приютино».\n\n" +
                        "📄 Пожалуйста, ознакомьтесь с документом выше.\n\n" +
                        "Нажимая кнопку «Зарегистрироваться», вы даёте согласие " +
                        "на обработку персональных данных (версия %s).",
                policyVersion
        );

        // 1. Отправляем документ
        documentSender.sendFromClasspath(telegramId, policyClasspath, policyFileName, caption);

        // 2. Отправляем клавиатуру отдельным сообщением (так надежнее в Telegram API)
        // telegramGateway.sendMessage(telegramId, "Выберите действие:", InlineKeyboardFactory.createRegistrationStartKeyboard());
        // Примечание: вызов gateway лучше вынести в StartCommandHandler,
        // а здесь вернуть саму клавиатуру или делегировать.
        // Для простоты оставим отправку клавиатуры на усмотрение вызывающего хендлера.
    }

    public String getPolicyVersion() {
        return policyVersion;
    }
}