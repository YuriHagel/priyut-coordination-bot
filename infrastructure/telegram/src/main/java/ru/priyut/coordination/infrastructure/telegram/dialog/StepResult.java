package ru.priyut.coordination.infrastructure.telegram.dialog;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;

/**
 * Результат обработки шага диалога.
 * Может содержать:
 * - сообщение об ошибке (если ввод невалиден)
 * - следующий шаг (если переходим дальше)
 * - сигнал завершения диалога
 */
public sealed interface StepResult {

    /**
     * Ввод невалиден — показываем ошибку и остаёмся на текущем шаге.
     */
    record ValidationError(SendMessage errorMessage) implements StepResult {
        public static ValidationError of(String text) {
            var msg = new SendMessage();
            msg.setText(text);
            return new ValidationError(msg);
        }
    }

    /**
     * Шаг успешно обработан — переходим к следующему.
     */
    record Success(SendMessage nextMessage) implements StepResult {
        public static Success of(SendMessage message) {
            return new Success(message);
        }

        public static Success empty() {
            return new Success(null);
        }
    }

    /**
     * Диалог завершён.
     */
    record Completed(SendMessage finalMessage) implements StepResult {
        public static Completed of(String text) {
            var msg = new SendMessage();
            msg.setText(text);
            return new Completed(msg);
        }
    }

    /**
     * Пользователь отменил диалог.
     */
    record Cancelled(SendMessage cancelMessage) implements StepResult {
        public static Cancelled of(String text) {
            var msg = new SendMessage();
            msg.setText(text);
            return new Cancelled(msg);
        }
    }
}