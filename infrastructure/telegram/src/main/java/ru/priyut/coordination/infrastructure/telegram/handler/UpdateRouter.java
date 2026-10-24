package ru.priyut.coordination.infrastructure.telegram.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.bot.FinalUpdateHandler;
import ru.priyut.coordination.infrastructure.telegram.callback.CallbackRouter;
import ru.priyut.coordination.infrastructure.telegram.dialog.ConversationContext;
import ru.priyut.coordination.infrastructure.telegram.dialog.ConversationStore;
import ru.priyut.coordination.infrastructure.telegram.dialog.TripCancelDialog;
import ru.priyut.coordination.infrastructure.telegram.dialog.TripCreationDialog;
import ru.priyut.coordination.infrastructure.telegram.dialog.VolunteerRegistrationDialog;

/**
 * Роутер верхнего уровня.
 * Решает, куда направить входящее обновление:
 * 1. В активный диалог (если есть)
 * 2. В CommandRouter (если команда)
 * 3. В CallbackRouter (если inline-кнопка)
 * 4. В fallback (если текст без команды)
 */
@Component
public class UpdateRouter implements FinalUpdateHandler {

    private static final Logger log = LoggerFactory.getLogger(UpdateRouter.class);

    private final CommandRouter commandRouter;
    private final CallbackRouter callbackRouter;
    private final ConversationStore conversationStore;
    private final VolunteerRegistrationDialog registrationDialog;
    private final TripCreationDialog tripCreationDialog;
    private final TripCancelDialog tripCancelDialog;

    public UpdateRouter(
            CommandRouter commandRouter,
            CallbackRouter callbackRouter,
            ConversationStore conversationStore,
            VolunteerRegistrationDialog registrationDialog,
            TripCreationDialog tripCreationDialog,
            TripCancelDialog tripCancelDialog
    ) {
        this.commandRouter = commandRouter;
        this.callbackRouter = callbackRouter;
        this.conversationStore = conversationStore;
        this.registrationDialog = registrationDialog;
        this.tripCreationDialog = tripCreationDialog;
        this.tripCancelDialog = tripCancelDialog;
    }

    @Override
    public void handle(Update update) {
        var telegramId = extractTelegramId(update);
        if (telegramId == null) {
            log.warn("Cannot extract Telegram ID from update_id={}", update.getUpdateId());
            return;
        }

        // 1. Проверяем активный диалог (приоритет выше команд)
        var dialogOpt = conversationStore.load(telegramId);
        if (dialogOpt.isPresent()) {
            var context = dialogOpt.get();

            // Проверка TTL (30 минут неактивности)
            if (context.isExpired(java.time.Instant.now(), java.time.Duration.ofMinutes(30))) {
                conversationStore.delete(telegramId);
                log.info("Expired dialog {} for user {}", context.getDialogType(), telegramId);
            } else if (routeToDialog(telegramId, context, update)) {
                return; // Диалог обработал обновление
            }
        }

        // 2. Маршрутизация по типу обновления
        if (update.hasCallbackQuery()) {
            callbackRouter.handle(update);
        } else if (update.hasMessage() && update.getMessage().isCommand()) {
            commandRouter.handle(update);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(telegramId, update);
        } else {
            log.debug("Unhandled update type from user {}", telegramId);
        }
    }

    /**
     * Маршрутизация к нужному диалогу.
     */
    private boolean routeToDialog(TelegramId telegramId, ConversationContext context, Update update) {
        return switch (context.getDialogType()) {
            case VOLUNTEER_REGISTRATION -> registrationDialog.handleUpdate(telegramId, update);
            case TRIP_CREATION -> tripCreationDialog.handleUpdate(telegramId, update);
            case TRIP_CANCEL -> tripCancelDialog.handleUpdate(telegramId, update);
        };
    }

    /**
     * Обработка текстового сообщения без команды.
     */
    private void handleTextMessage(TelegramId telegramId, Update update) {
        var text = update.getMessage().getText().trim();

        // Если пользователь ввёл "/start" без слеша — обрабатываем как команду
        if ("start".equalsIgnoreCase(text)) {
            commandRouter.handle(update);
            return;
        }

        log.debug("Received text message without command from user {}: {}", telegramId, text);

        // Отправляем подсказку
        var msg = new org.telegram.telegrambots.meta.api.methods.send.SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText("👋 Я понимаю только команды.\n\nОтправьте /start для начала работы.");
        // telegramGateway.execute(msg); // раскомментируйте, когда gateway будет готов
    }

    private TelegramId extractTelegramId(Update update) {
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            return TelegramId.of(update.getMessage().getFrom().getId());
        }
        if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            return TelegramId.of(update.getCallbackQuery().getFrom().getId());
        }
        return null;
    }
}