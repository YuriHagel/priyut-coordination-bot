package ru.priyut.coordination.domain.admin;

import ru.priyut.coordination.domain.shared.DomainException;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Белый список администраторов.
 * Value object: неизменяемая коллекция с бизнес-правилами.
 */
public final class AdminWhitelist {

    private final Map<TelegramId, AdminUser> admins;

    private AdminWhitelist(Map<TelegramId, AdminUser> admins) {
        this.admins = Collections.unmodifiableMap(new HashMap<>(admins));
    }

    public static AdminWhitelist empty() {
        return new AdminWhitelist(Map.of());
    }

    public static AdminWhitelist of(Iterable<AdminUser> admins) {
        Map<TelegramId, AdminUser> map = new HashMap<>();
        for (AdminUser admin : admins) {
            map.put(admin.telegramId(), admin);
        }
        return new AdminWhitelist(map);
    }

    /**
     * Проверка, является ли пользователь администратором (сценарий N5).
     */
    public boolean isAdmin(TelegramId telegramId) {
        return admins.containsKey(telegramId);
    }

    /**
     * Получение информации об администраторе.
     */
    public Optional<AdminUser> findByTelegramId(TelegramId telegramId) {
        return Optional.ofNullable(admins.get(telegramId));
    }

    /**
     * Требование наличия администратора (бросает исключение, если не админ).
     */
    public AdminUser requireAdmin(TelegramId telegramId) {
        return findByTelegramId(telegramId)
                .orElseThrow(() -> new DomainException(
                        "User with telegram ID " + telegramId + " is not an administrator"));
    }

    /**
     * Добавление нового администратора.
     */
    public AdminWhitelist add(AdminUser admin) {
        Objects.requireNonNull(admin, "admin must not be null");
        Map<TelegramId, AdminUser> newMap = new HashMap<>(admins);
        newMap.put(admin.telegramId(), admin);
        return new AdminWhitelist(newMap);
    }

    /**
     * Удаление администратора.
     */
    public AdminWhitelist remove(TelegramId telegramId) {
        Map<TelegramId, AdminUser> newMap = new HashMap<>(admins);
        newMap.remove(telegramId);
        return new AdminWhitelist(newMap);
    }

    /**
     * Количество администраторов.
     */
    public int size() {
        return admins.size();
    }

    /**
     * Все администраторы (неизменяемая коллекция).
     */
    public Iterable<AdminUser> all() {
        return admins.values();
    }
}