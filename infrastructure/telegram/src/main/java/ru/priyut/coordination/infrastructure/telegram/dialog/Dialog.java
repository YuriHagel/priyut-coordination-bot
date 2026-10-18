package ru.priyut.coordination.infrastructure.telegram.dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.shared.UtcClock;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGateway;

import java.util.List;

/**
 * Базовый класс для диалогов.
 * Управляет последовательностью шагов и жизненным циклом.
 */
public abstract class Dialog {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final ConversationStore conversationStore;
    protected final TelegramBotGateway telegramGateway;
    protected final UtcClock clock;

    protected Dialog(
            ConversationStore conversationStore,
            TelegramBotGateway telegramGateway,
            UtcClock clock
    ) {
        this.conversationStore = conversationStore;
        this.telegramGateway = telegramGateway;
        this.clock = clock;
    }

    /**
     * Тип диалога (для ConversationContext).
     */
    protected abstract ConversationContext.DialogType dialogType();

    /**
     * Список шагов диалога в порядке выполнения.
     */
    protected abstract List<DialogStep> steps();

    /**
     * Запуск нового диалога.
     */
    public void start(TelegramId telegramId) {
        // Если уже есть активный диалог — отменяем его
        conversationStore.load(telegramId).ifPresent(ctx -> {
            log.info("Cancelling existing dialog {} for user {}", ctx.getDialogType(), telegramId);
            conversationStore.delete(telegramId);
        });

        var context = ConversationContext.start(telegramId, dialogType(), clock.now());
        conversationStore.save(context);

        // Рендерим первый шаг
        var firstStep = steps().get(0);
        var message = firstStep.render(context);
        sendMessage(telegramId, message);

        log.info("Started dialog {} for user {}", dialogType(), telegramId);
    }

    /**
     * Обработка входящего сообщения в рамках активного диалога.
     *
     * @return true, если сообщение было обработано диалогом
     */
    public boolean handleUpdate(TelegramId telegramId, Update update) {
        var contextOpt = conversationStore.load(telegramId);
        if (contextOpt.isEmpty()) {
            return false;
        }

        var context = contextOpt.get();

        // Проверка типа диалога
        if (context.getDialogType() != dialogType()) {
            return false;
        }

        // Проверка на отмену (/cancel)
        if (isCancelCommand(update)) {
            handleCancel(telegramId, context);
            return true;
        }

        // Получаем текущий шаг
        var steps = steps();
        int stepIndex = context.getCurrentStepIndex();
        if (stepIndex >= steps.size()) {
            // Диалог уже завершён, но контекст не удалён — чистим
            conversationStore.delete(telegramId);
            return false;
        }

        var currentStep = steps.get(stepIndex);
        var result = currentStep.handle(update, context);

        return processResult(telegramId, context, currentStep, result);
    }

    /**
     * Обработка результата шага.
     */
    private boolean processResult(
            TelegramId telegramId,
            ConversationContext context,
            DialogStep currentStep,
            StepResult result
    ) {
        switch (result) {
            case StepResult.ValidationError(var errorMessage) -> {
                sendMessage(telegramId, errorMessage);
                return true;
            }
            case StepResult.Success(var nextMessage) -> {
                context.advanceToNextStep();
                var steps = steps();

                if (context.getCurrentStepIndex() >= steps.size()) {
                    // Диалог завершён
                    onComplete(telegramId, context);
                    conversationStore.delete(telegramId);
                    log.info("Completed dialog {} for user {}", dialogType(), telegramId);
                } else {
                    // Переходим к следующему шагу
                    var nextStep = steps.get(context.getCurrentStepIndex());
                    var stepMessage = nextStep.render(context);
                    sendMessage(telegramId, stepMessage);
                    if (nextMessage != null) {
                        sendMessage(telegramId, nextMessage);
                    }
                }
                conversationStore.save(context);
                return true;
            }
            case StepResult.Completed(var finalMessage) -> {
                onComplete(telegramId, context);
                conversationStore.delete(telegramId);
                if (finalMessage != null) {
                    sendMessage(telegramId, finalMessage);
                }
                log.info("Completed dialog {} for user {}", dialogType(), telegramId);
                return true;
            }
            case StepResult.Cancelled(var cancelMessage) -> {
                conversationStore.delete(telegramId);
                if (cancelMessage != null) {
                    sendMessage(telegramId, cancelMessage);
                }
                log.info("Cancelled dialog {} for user {}", dialogType(), telegramId);
                return true;
            }
        }
    }

    /**
     * Хук для подклассов — вызывается при успешном завершении диалога.
     * Здесь можно сохранить данные в БД, отправить уведомления и т.д.
     */
    protected abstract void onComplete(TelegramId telegramId, ConversationContext context);

    /**
     * Проверка, является ли сообщение командой отмены.
     */
    private boolean isCancelCommand(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var text = update.getMessage().getText().trim();
            return "/cancel".equalsIgnoreCase(text);
        }
        return false;
    }

    /**
     * Обработка отмены диалога.
     */
    private void handleCancel(TelegramId telegramId, ConversationContext context) {
        conversationStore.delete(telegramId);
        var msg = new SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText("❌ Диалог отменён. Все введённые данные потеряны.");
        sendMessage(telegramId, msg);
        log.info("User {} cancelled dialog {}", telegramId, dialogType());
    }

    /**
     * Отправка сообщения через gateway.
     */
    protected void sendMessage(TelegramId telegramId, SendMessage message) {
        if (message == null) return;
        message.setChatId(telegramId.value());
        telegramGateway.execute(message);
    }
}