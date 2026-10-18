package ru.priyut.coordination.infrastructure.telegram.dialog;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Один шаг диалога.
 * Отвечает за:
 * - рендеринг вопроса/сообщения
 * - обработку ответа пользователя
 * - валидацию ввода
 * - переход к следующему шагу
 */
public interface DialogStep {

    /**
     * Идентификатор шага (для логирования и метрик).
     */
    String name();

    /**
     * Рендерит сообщение-вопрос для пользователя.
     * Вызывается при входе в шаг.
     */
    SendMessage render(ConversationContext context);

    /**
     * Обрабатывает ответ пользователя.
     *
     * @param update  входящее обновление от Telegram
     * @param context текущее состояние диалога
     * @return результат обработки
     */
    StepResult handle(Update update, ConversationContext context);

    /**
     * Можно ли пропустить этот шаг (опционально).
     */
    default boolean isOptional() {
        return false;
    }
}