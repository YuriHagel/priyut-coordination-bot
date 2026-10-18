package ru.priyut.coordination.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import ru.priyut.coordination.application.port.outbound.AdminWhitelistRepository;
import ru.priyut.coordination.domain.admin.AdminUser;
import ru.priyut.coordination.domain.admin.AdminWhitelist;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.persistence.repository.JdbcAdminWhitelistRepository;

import java.util.Optional;

@Component
public class AdminWhitelistPersistenceAdapter implements AdminWhitelistRepository {

    private final JdbcAdminWhitelistRepository jdbcRepository;

    public AdminWhitelistPersistenceAdapter(JdbcAdminWhitelistRepository jdbcRepository) {
        this.jdbcRepository = jdbcRepository;
    }

    @Override
    public AdminWhitelist loadAll() {
        return jdbcRepository.loadAll();
    }

    @Override
    public Optional<AdminUser> findByTelegramId(TelegramId telegramId) {
        return jdbcRepository.findByTelegramId(telegramId);
    }

    @Override
    public AdminUser save(AdminUser admin) {
        return jdbcRepository.save(admin);
    }

    @Override
    public void deleteByTelegramId(TelegramId telegramId) {
        jdbcRepository.deleteByTelegramId(telegramId);
    }
}