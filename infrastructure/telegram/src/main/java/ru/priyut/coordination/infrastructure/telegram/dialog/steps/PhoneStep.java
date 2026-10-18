package ru.priyut.coordination.infrastructure.telegram.dialog.steps;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.infrastructure.telegram.dialog.ConversationContext;
import ru.priyut.coordination.infrastructure.telegram.dialog.DialogStep;
import ru.priyut.coordination.infrastructure.telegram.dialog.StepResult;

/**
 * Шаг: ввод телефона с валидацией формата +7XXXXXXXXXX.
 */
public class PhoneStep implements DialogStep {

    private static final String PHONE_REGEX = "^\\+7\\d{10}$";

    @Override
    public String name() {
        return "PHONE";
    }

    @Override
    public SendMessage render(ConversationContext context) {
        var msg = new SendMessage();
        msg.setText("📞 Ваш номер телефона для связи?\n\nФормат: +7XXXXXXXXXX\nПример: +79991234567");
        return msg;
    }

    @Override
    public StepResult handle(Update update, ConversationContext context) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return StepResult.ValidationError.of("Пожалуйста, отправьте текстовое сообщение.");
        }

        var rawPhone = update.getMessage().getText().trim();
        // Нормализация: убираем пробелы, дефисы, скобки
        var normalized = rawPhone.replaceAll("[\\s\\-()]", "");

        if (!normalized.matches(PHONE_REGEX)) {
            return StepResult.ValidationError.of(
                    "❌ Неверный формат телефона.\n\nОжидается: +7XXXXXXXXXX\nПример: +79991234567"
            );
        }

        context.put("phone", normalized);
        return StepResult.empty();
    }
}