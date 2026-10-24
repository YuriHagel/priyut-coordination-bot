package ru.priyut.coordination.infrastructure.telegram.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGateway;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Маршрутизатор команд.
 * Находит подходящий CommandHandler по тексту команды и вызывает его.
 */
@Component
public class CommandRouter {

    private static final Logger log = LoggerFactory.getLogger(CommandRouter.class);

    private final Map<String, CommandHandler> handlersByCommand;
    private final TelegramBotGateway telegramGateway;

    public CommandRouter(List<CommandHandler> handlers, TelegramBotGateway telegramGateway) {
        this.handlersByCommand = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        h -> h.command().toLowerCase(),
                        Function.identity()
                ));
        this.telegramGateway = telegramGateway;

        log.info("✅ CommandRouter initialized with {} handlers: {}",
                handlers.size(), handlersByCommand.keySet());
    }

    /**
     * Обработка команды.
     */
    public void handle(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        var message = update.getMessage();
        var text = message.getText().trim();
        var commandText = text.split("\\s+")[0].toLowerCase();
        var telegramId = TelegramId.of(message.getFrom().getId());

        var handler = handlersByCommand.get(commandText);
        if (handler == null) {
            log.debug("Unknown command '{}' from user {}", commandText, telegramId);
            sendUnknownCommandMessage(telegramId, commandText);
            return;
        }

        log.info("Routing command '{}' to {} for user {}",
                commandText, handler.getClass().getSimpleName(), telegramId);

        try {
            handler.handle(update, telegramId);
        } catch (Exception e) {
            log.error("Error handling command '{}' from user {}", commandText, telegramId, e);
            sendErrorMessage(telegramId);
        }
    }

    /**
     * Проверка, зарегистрирован ли handler для команды.
     */
    public boolean hasHandler(String command) {
        return handlersByCommand.containsKey(command.toLowerCase());
    }

    private void sendUnknownCommandMessage(TelegramId telegramId, String command) {
        var msg = new org.telegram.telegrambots.meta.api.methods.send.SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText("❓ Неизвестная команда: " + command + "\n\n" +
                "Используйте /start для начала работы.");
        telegramGateway.execute(msg);
    }

    private void sendErrorMessage(TelegramId telegramId) {
        var msg = new org.telegram.telegrambots.meta.api.methods.send.SendMessage();
        msg.setChatId(telegramId.value());
        msg.setText("❌ Произошла ошибка при обработке команды. Попробуйте позже.");
        telegramGateway.execute(msg);
    }
}