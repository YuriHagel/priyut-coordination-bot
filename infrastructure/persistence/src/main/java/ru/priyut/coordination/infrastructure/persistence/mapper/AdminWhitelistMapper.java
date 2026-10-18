package ru.priyut.coordination.infrastructure.persistence.mapper;

import ru.priyut.coordination.domain.admin.AdminUser;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.persistence.record.AdminWhitelistRecord;

/**
 * Маппер между доменным объектом AdminUser и AdminWhitelistRecord.
 */
public final class AdminWhitelistMapper {

    private AdminWhitelistMapper() {
    }

    public static AdminWhitelistRecord toRecord(AdminUser admin) {
        return new AdminWhitelistRecord(
                admin.telegramId().value(),
                admin.displayName(),
                null, // addedAt не используется при сохранении
                null  // addedBy не используется при сохранении
        );
    }

    public static AdminUser toDomain(AdminWhitelistRecord record) {
        return AdminUser.of(
                TelegramId.of(record.telegramId()),
                record.displayName()
        );
    }
}