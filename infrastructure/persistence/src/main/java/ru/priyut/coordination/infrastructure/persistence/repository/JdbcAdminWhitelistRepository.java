package ru.priyut.coordination.infrastructure.persistence.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.priyut.coordination.domain.admin.AdminUser;
import ru.priyut.coordination.domain.admin.AdminWhitelist;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.persistence.mapper.AdminWhitelistMapper;
import ru.priyut.coordination.infrastructure.persistence.record.AdminWhitelistRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcAdminWhitelistRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcAdminWhitelistRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AdminWhitelist loadAll() {
        var sql = "SELECT * FROM admin_whitelist";
        var records = jdbcTemplate.query(sql, new AdminWhitelistRowMapper());
        var admins = records.stream().map(AdminWhitelistMapper::toDomain).toList();
        return AdminWhitelist.of(admins);
    }

    public Optional<AdminUser> findByTelegramId(TelegramId telegramId) {
        var sql = "SELECT * FROM admin_whitelist WHERE telegram_id = :telegramId";
        var params = new MapSqlParameterSource().addValue("telegramId", telegramId.value());
        var records = jdbcTemplate.query(sql, params, new AdminWhitelistRowMapper());
        return records.stream().map(AdminWhitelistMapper::toDomain).findFirst();
    }

    public AdminUser save(AdminUser admin) {
        var record = AdminWhitelistMapper.toRecord(admin);
        var sql = """
                INSERT INTO admin_whitelist (
                    telegram_id, display_name
                ) VALUES (
                    :telegramId, :displayName
                )
                ON CONFLICT (telegram_id) DO UPDATE SET
                    display_name = EXCLUDED.display_name
                """;

        var params = new MapSqlParameterSource()
                .addValue("telegramId", record.telegramId())
                .addValue("displayName", record.displayName());

        jdbcTemplate.update(sql, params);
        return admin;
    }

    public void deleteByTelegramId(TelegramId telegramId) {
        var sql = "DELETE FROM admin_whitelist WHERE telegram_id = :telegramId";
        var params = new MapSqlParameterSource().addValue("telegramId", telegramId.value());
        jdbcTemplate.update(sql, params);
    }

    private static class AdminWhitelistRowMapper implements RowMapper<AdminWhitelistRecord> {
        @Override
        public AdminWhitelistRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AdminWhitelistRecord(
                    rs.getLong("telegram_id"),
                    rs.getString("display_name"),
                    rs.getTimestamp("added_at").toInstant(),
                    getLongOrNull(rs, "added_by")
            );
        }

        private Long getLongOrNull(ResultSet rs, String column) throws SQLException {
            var value = rs.getObject(column);
            return value != null ? rs.getLong(column) : null;
        }
    }
}