package ru.priyut.coordination.infrastructure.persistence.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.volunteer.Volunteer;
import ru.priyut.coordination.domain.volunteer.VolunteerId;
import ru.priyut.coordination.domain.volunteer.VolunteerStatus;
import ru.priyut.coordination.infrastructure.persistence.mapper.VolunteerMapper;
import ru.priyut.coordination.infrastructure.persistence.record.VolunteerRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcVolunteerRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcVolunteerRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Volunteer> findById(VolunteerId id) {
        var sql = "SELECT * FROM volunteers WHERE id = :id";
        var params = new MapSqlParameterSource().addValue("id", id.value());
        var records = jdbcTemplate.query(sql, params, new VolunteerRowMapper());
        return records.stream().map(VolunteerMapper::toDomain).findFirst();
    }

    public Optional<Volunteer> findByTelegramId(TelegramId telegramId) {
        var sql = "SELECT * FROM volunteers WHERE telegram_id = :telegramId";
        var params = new MapSqlParameterSource().addValue("telegramId", telegramId.value());
        var records = jdbcTemplate.query(sql, params, new VolunteerRowMapper());
        return records.stream().map(VolunteerMapper::toDomain).findFirst();
    }

    public List<Volunteer> findByStatus(VolunteerStatus status) {
        var sql = "SELECT * FROM volunteers WHERE status = :status";
        var params = new MapSqlParameterSource().addValue("status", status.name());
        var records = jdbcTemplate.query(sql, params, new VolunteerRowMapper());
        return records.stream().map(VolunteerMapper::toDomain).toList();
    }

    public Volunteer save(Volunteer volunteer) {
        var record = VolunteerMapper.toRecord(volunteer);
        var sql = """
                INSERT INTO volunteers (
                    id, telegram_id, full_name, phone, district,
                    car_brand, car_model, car_color,
                    availability_days, availability_time_from, availability_time_to, availability_status,
                    status, created_at, updated_at, last_activity_at
                ) VALUES (
                    :id, :telegramId, :fullName, :phone, :district,
                    :carBrand, :carModel, :carColor,
                    :availabilityDays, :availabilityTimeFrom, :availabilityTimeTo, :availabilityStatus,
                    :status, :createdAt, :updatedAt, :lastActivityAt
                )
                ON CONFLICT (id) DO UPDATE SET
                    full_name = EXCLUDED.full_name,
                    phone = EXCLUDED.phone,
                    district = EXCLUDED.district,
                    car_brand = EXCLUDED.car_brand,
                    car_model = EXCLUDED.car_model,
                    car_color = EXCLUDED.car_color,
                    availability_days = EXCLUDED.availability_days,
                    availability_time_from = EXCLUDED.availability_time_from,
                    availability_time_to = EXCLUDED.availability_time_to,
                    availability_status = EXCLUDED.availability_status,
                    status = EXCLUDED.status,
                    updated_at = EXCLUDED.updated_at,
                    last_activity_at = EXCLUDED.last_activity_at
                """;

        var params = new MapSqlParameterSource()
                .addValue("id", record.id())
                .addValue("telegramId", record.telegramId())
                .addValue("fullName", record.fullName())
                .addValue("phone", record.phone())
                .addValue("district", record.district())
                .addValue("carBrand", record.carBrand())
                .addValue("carModel", record.carModel())
                .addValue("carColor", record.carColor())
                .addValue("availabilityDays", record.availabilityDays())
                .addValue("availabilityTimeFrom", record.availabilityTimeFrom())
                .addValue("availabilityTimeTo", record.availabilityTimeTo())
                .addValue("availabilityStatus", record.availabilityStatus())
                .addValue("status", record.status())
                .addValue("createdAt", record.createdAt())
                .addValue("updatedAt", record.updatedAt())
                .addValue("lastActivityAt", record.lastActivityAt());

        jdbcTemplate.update(sql, params);
        return volunteer;
    }

    public void deleteById(VolunteerId id) {
        var sql = "DELETE FROM volunteers WHERE id = :id";
        var params = new MapSqlParameterSource().addValue("id", id.value());
        jdbcTemplate.update(sql, params);
    }

    private static class VolunteerRowMapper implements RowMapper<VolunteerRecord> {
        @Override
        public VolunteerRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new VolunteerRecord(
                    (java.util.UUID) rs.getObject("id"),
                    rs.getLong("telegram_id"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("district"),
                    rs.getString("car_brand"),
                    rs.getString("car_model"),
                    rs.getString("car_color"),
                    rs.getString("availability_days"),
                    rs.getTime("availability_time_from").toLocalTime(),
                    rs.getTime("availability_time_to").toLocalTime(),
                    rs.getString("availability_status"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant(),
                    rs.getTimestamp("last_activity_at").toInstant()
            );
        }
    }
}