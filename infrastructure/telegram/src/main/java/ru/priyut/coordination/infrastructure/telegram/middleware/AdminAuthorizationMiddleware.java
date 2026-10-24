package ru.priyut.coordination.infrastructure.telegram.middleware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.config.TelegramBotProperties;
import ru.priyut.coordination.infrastructure.telegram.gateway.TelegramBotGateway;

/**
 * Middleware для проверки прав администратора.
 * Выполняется четвёртым (порядок 40).
 *
 * Проверяет:
 * - Если обновление содержит админскую команду (/new_trip, /cancel_trip и т.д.)
 * - Telegram ID пользователя должен быть в белом списке
 *
 * Если пользователь не админ — отправляет сообщение об ошибке и прерывает цепочку.
 */
@Component
public class AdminAuthorizationMiddleware implements Middleware {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthorizationMiddleware.class);

    private final TelegramBotProperties properties;
    private final TelegramBotGateway telegramGateway;

    public AdminAuthorizationMiddleware(
            TelegramBotProperties properties,
            TelegramBotGateway telegramGateway
    ) {
        this.properties = properties;
        this.telegramGateway = telegramGateway;
    }

    @Override
    public void handle(Update update, UpdateChain chain) {
        // Проверяем, является ли это админской командой
        if (!isAdminCommand(update)) {
            chain.proceed(update); // Не админская команда — пропускаем проверку
            return;
        }

        // Извлекаем Telegram ID
        var telegramId = extractTelegramId(update);
        if (telegramId == null) {
            log.warn("Cannot extract Telegram ID for admin command check");
            chain.proceed(update);
            return;
        }

        // Проверяем белый список
        var adminIds = properties.getBot().getAdminIdsAsSet();
        if (!adminIds.contains(telegramId.value())) {
            log.warn("🚫 Unauthorized admin command from user {}", maskTelegramId(telegramId.value()));

            // Отправляем сообщение об ошибке
            var msg = new org.telegram.telegrambots.meta.api.methods.send.SendMessage();
            msg.setChatId(telegramId.value());
            msg.setText("❌ У вас нет прав администратора для выполнения этой команды.");
            telegramGateway.execute(msg);

            return; // Прерываем цепочку
        }

        log.debug("✅ Admin command authorized for user {}", maskTelegramId(telegramId.value()));
        chain.proceed(update);
    }

    @Override
    public int order() {
        return 40; // Четвёртый middleware
    }

    /**
     * Проверка, является ли команда админской.
     */
    private boolean isAdminCommand(Update update) {
        if (!update.hasMessage() || !update.getMessage().isCommand()) {
            return false;
        }

        var command = update.getMessage().getText().split("\\s+")[0].toLowerCase();
        return command.equals("/new_trip")
                || command.equals("/cancel_trip")
                || command.equals("/change_time")
                || command.equals("/export_trips")
                || command.equals("/approve_volunteer")
                || command.equals("/reject_volunteer");
    }

    private TelegramId extractTelegramId(Update update) {
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            return TelegramId.of(update.getMessage().getFrom().getId());
        }
        return null;
    }

    private String maskTelegramId(Long telegramId) {
        if (telegramId == null) return "null";
        var idStr = telegramId.toString();
        if (idStr.length() <= 4) return "***";
        return idStr.substring(0, 2) + "***" + idStr.substring(idStr.length() - 2);
    }
}