package ru.priyut.coordination.application.port.outbound;

import ru.priyut.coordination.domain.admin.AdminUser;
import ru.priyut.coordination.domain.admin.AdminWhitelist;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.util.Optional;

/**
 * Порт для работы с белым списком администраторов.
 */
public interface AdminWhitelistRepository {

    AdminWhitelist loadAll();

    Optional<AdminUser> findByTelegramId(TelegramId telegramId);

    AdminUser save(AdminUser admin);

    void deleteByTelegramId(TelegramId telegramId);
}