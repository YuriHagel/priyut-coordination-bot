package ru.priyut.coordination.infrastructure.telegram.dialog.steps;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.infrastructure.telegram.dialog.ConversationContext;
import ru.priyut.coordination.infrastructure.telegram.dialog.DialogStep;
import ru.priyut.coordination.infrastructure.telegram.dialog.StepResult;

/**
 * Шаг: ввод имени волонтёра.
 */
public class NameStep implements DialogStep {

    @Override
    public String name() {
        return "NAME";
    }

    @Override
    public SendMessage render(ConversationContext context) {
        var msg = new SendMessage();
        msg.setText("👤 Как вас зовут?\n\nВведите ваше имя полностью.");
        return msg;
    }

    @Override
    public StepResult handle(Update update, ConversationContext context) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return StepResult.ValidationError.of("Пожалуйста, отправьте текстовое сообщение.");
        }

        var name = update.getMessage().getText().trim();

        if (name.isEmpty()) {
            return StepResult.ValidationError.of("Имя не может быть пустым. Попробуйте ещё раз.");
        }

        if (name.length() > 100) {
            return StepResult.ValidationError.of("Имя слишком длинное. Максимум 100 символов.");
        }

        if (name.length() < 2) {
            return StepResult.ValidationError.of("Имя слишком короткое. Минимум 2 символа.");
        }

        // Сохраняем в контекст
        context.put("fullName", name);

        return StepResult.empty();
    }
}