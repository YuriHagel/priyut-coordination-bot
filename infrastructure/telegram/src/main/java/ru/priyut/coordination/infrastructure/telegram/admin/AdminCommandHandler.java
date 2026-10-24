package ru.priyut.coordination.infrastructure.telegram.admin;

import ru.priyut.coordination.infrastructure.telegram.handler.CommandHandler;

/**
 * Маркерный интерфейс для админских команд.
 *
 * Все handler'ы, реализующие этот интерфейс, автоматически проверяются
 * через AdminAuthorizationMiddleware на наличие Telegram ID в белом списке.
 *
 * Это архитектурный контракт: если handler — админский, он ДОЛЖЕН
 * реализовывать этот интерфейс.
 */
public interface AdminCommandHandler extends CommandHandler {
    // Маркерный интерфейс — не добавляет новых методов.
    // Семантика: "этот handler требует прав администратора".
}