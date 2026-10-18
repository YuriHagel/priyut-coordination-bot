package ru.priyut.coordination.infrastructure.persistence.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.priyut.coordination.domain.trip.TripId;
import ru.priyut.coordination.infrastructure.persistence.record.TripHistoryRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcTripHistoryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcTripHistoryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveEvent(TripId tripId, String eventType, Long actorTelegramId, String payload) {
        var sql = """
                INSERT INTO trip_history (
                    trip_uuid, event_type, actor_telegram_id, payload, occurred_at
                ) VALUES (
                    :tripUuid, :eventType, :actorTelegramId, :payload, NOW()
                )
                """;

        var params = new MapSqlParameterSource()
                .addValue("tripUuid", tripId.value())
                .addValue("eventType", eventType)
                .addValue("actorTelegramId", actorTelegramId)
                .addValue("payload", payload);

        jdbcTemplate.update(sql, params);
    }

    public List<TripHistoryRecord> findEventsByTripId(TripId tripId) {
        var sql = "SELECT * FROM trip_history WHERE trip_uuid = :tripUuid ORDER BY occurred_at DESC";
        var params = new MapSqlParameterSource().addValue("tripUuid", tripId.value());
        return jdbcTemplate.query(sql, params, new TripHistoryRowMapper());
    }

    private static class TripHistoryRowMapper implements RowMapper<TripHistoryRecord> {
        @Override
        public TripHistoryRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TripHistoryRecord(
                    rs.getLong("id"),
                    (UUID) rs.getObject("trip_uuid"),
                    rs.getString("event_type"),
                    getLongOrNull(rs, "actor_telegram_id"),
                    rs.getString("payload"),
                    rs.getTimestamp("occurred_at").toInstant()
            );
        }

        private Long getLongOrNull(ResultSet rs, String column) throws SQLException {
            var value = rs.getObject(column);
            return value != null ? rs.getLong(column) : null;
        }
    }
}