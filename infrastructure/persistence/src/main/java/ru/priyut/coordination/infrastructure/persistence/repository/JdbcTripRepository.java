package ru.priyut.coordination.infrastructure.persistence.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.domain.trip.TripId;
import ru.priyut.coordination.domain.trip.TripStatus;
import ru.priyut.coordination.domain.volunteer.VolunteerId;
import ru.priyut.coordination.infrastructure.persistence.mapper.TripMapper;
import ru.priyut.coordination.infrastructure.persistence.record.TripRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcTripRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcTripRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Trip> findById(Long id) {
        var sql = "SELECT * FROM trips WHERE id = :id";
        var params = new MapSqlParameterSource().addValue("id", id);
        var records = jdbcTemplate.query(sql, params, new TripRowMapper());
        return records.stream().map(TripMapper::toDomain).findFirst();
    }

    public Optional<Trip> findByUuid(UUID uuid) {
        var sql = "SELECT * FROM trips WHERE uuid = :uuid";
        var params = new MapSqlParameterSource().addValue("uuid", uuid);
        var records = jdbcTemplate.query(sql, params, new TripRowMapper());
        return records.stream().map(TripMapper::toDomain).findFirst();
    }

    public List<Trip> findByStatus(TripStatus status) {
        var sql = "SELECT * FROM trips WHERE status = :status";
        var params = new MapSqlParameterSource().addValue("status", status.name());
        var records = jdbcTemplate.query(sql, params, new TripRowMapper());
        return records.stream().map(TripMapper::toDomain).toList();
    }

    public List<Trip> findUpcomingTrips(LocalDateTime from, LocalDateTime to) {
        var sql = "SELECT * FROM trips WHERE trip_date_time BETWEEN :from AND :to";
        var params = new MapSqlParameterSource()
                .addValue("from", from)
                .addValue("to", to);
        var records = jdbcTemplate.query(sql, params, new TripRowMapper());
        return records.stream().map(TripMapper::toDomain).toList();
    }

    public List<Trip> findByAssignedVolunteerId(VolunteerId volunteerId) {
        var sql = "SELECT * FROM trips WHERE assigned_volunteer_uuid = :volunteerUuid";
        var params = new MapSqlParameterSource().addValue("volunteerUuid", volunteerId.value());
        var records = jdbcTemplate.query(sql, params, new TripRowMapper());
        return records.stream().map(TripMapper::toDomain).toList();
    }

    public Trip save(Trip trip) {
        var record = TripMapper.toRecord(trip);
        var sql = """
                INSERT INTO trips (
                    id, uuid, created_by_admin_telegram_id,
                    route_from, route_to, route_destination,
                    trip_date_time, task_description, trip_type, comment,
                    status, assigned_volunteer_uuid, assigned_by_admin_telegram_id, assigned_at,
                    published_at, cancelled_at, cancel_reason,
                    completed_at, closed_by_admin_telegram_id,
                    created_at, updated_at, version
                ) VALUES (
                    :id, :uuid, :createdByAdminTelegramId,
                    :routeFrom, :routeTo, :routeDestination,
                    :tripDateTime, :taskDescription, :tripType, :comment,
                    :status, :assignedVolunteerUuid, :assignedByAdminTelegramId, :assignedAt,
                    :publishedAt, :cancelledAt, :cancelReason,
                    :completedAt, :closedByAdminTelegramId,
                    :createdAt, :updatedAt, :version
                )
                ON CONFLICT (uuid) DO UPDATE SET
                    route_from = EXCLUDED.route_from,
                    route_to = EXCLUDED.route_to,
                    route_destination = EXCLUDED.route_destination,
                    trip_date_time = EXCLUDED.trip_date_time,
                    task_description = EXCLUDED.task_description,
                    trip_type = EXCLUDED.trip_type,
                    comment = EXCLUDED.comment,
                    status = EXCLUDED.status,
                    assigned_volunteer_uuid = EXCLUDED.assigned_volunteer_uuid,
                    assigned_by_admin_telegram_id = EXCLUDED.assigned_by_admin_telegram_id,
                    assigned_at = EXCLUDED.assigned_at,
                    published_at = EXCLUDED.published_at,
                    cancelled_at = EXCLUDED.cancelled_at,
                    cancel_reason = EXCLUDED.cancel_reason,
                    completed_at = EXCLUDED.completed_at,
                    closed_by_admin_telegram_id = EXCLUDED.closed_by_admin_telegram_id,
                    updated_at = EXCLUDED.updated_at,
                    version = EXCLUDED.version
                """;

        var params = new MapSqlParameterSource()
                .addValue("id", record.id())
                .addValue("uuid", record.uuid())
                .addValue("createdByAdminTelegramId", record.createdByAdminTelegramId())
                .addValue("routeFrom", record.routeFrom())
                .addValue("routeTo", record.routeTo())
                .addValue("routeDestination", record.routeDestination())
                .addValue("tripDateTime", record.tripDateTime())
                .addValue("taskDescription", record.taskDescription())
                .addValue("tripType", record.tripType())
                .addValue("comment", record.comment())
                .addValue("status", record.status())
                .addValue("assignedVolunteerUuid", record.assignedVolunteerUuid())
                .addValue("assignedByAdminTelegramId", record.assignedByAdminTelegramId())
                .addValue("assignedAt", record.assignedAt())
                .addValue("publishedAt", record.publishedAt())
                .addValue("cancelledAt", record.cancelledAt())
                .addValue("cancelReason", record.cancelReason())
                .addValue("completedAt", record.completedAt())
                .addValue("closedByAdminTelegramId", record.closedByAdminTelegramId())
                .addValue("createdAt", record.createdAt())
                .addValue("updatedAt", record.updatedAt())
                .addValue("version", record.version());

        jdbcTemplate.update(sql, params);
        return trip;
    }

    public void deleteById(Long id) {
        var sql = "DELETE FROM trips WHERE id = :id";
        var params = new MapSqlParameterSource().addValue("id", id);
        jdbcTemplate.update(sql, params);
    }

    private static class TripRowMapper implements RowMapper<TripRecord> {
        @Override
        public TripRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            var assignedVolunteerUuid = rs.getObject("assigned_volunteer_uuid") != null
                    ? (UUID) rs.getObject("assigned_volunteer_uuid")
                    : null;

            return new TripRecord(
                    rs.getLong("id"),
                    (UUID) rs.getObject("uuid"),
                    rs.getLong("created_by_admin_telegram_id"),
                    rs.getString("route_from"),
                    rs.getString("route_to"),
                    rs.getString("route_destination"),
                    rs.getTimestamp("trip_date_time").toLocalDateTime(),
                    rs.getString("task_description"),
                    rs.getString("trip_type"),
                    rs.getString("comment"),
                    rs.getString("status"),
                    assignedVolunteerUuid,
                    getLongOrNull(rs, "assigned_by_admin_telegram_id"),
                    getInstantOrNull(rs, "assigned_at"),
                    getInstantOrNull(rs, "published_at"),
                    getInstantOrNull(rs, "cancelled_at"),
                    rs.getString("cancel_reason"),
                    getInstantOrNull(rs, "completed_at"),
                    getLongOrNull(rs, "closed_by_admin_telegram_id"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant(),
                    rs.getLong("version")
            );
        }

        private Long getLongOrNull(ResultSet rs, String column) throws SQLException {
            var value = rs.getObject(column);
            return value != null ? rs.getLong(column) : null;
        }

        private java.time.Instant getInstantOrNull(ResultSet rs, String column) throws SQLException {
            var timestamp = rs.getTimestamp(column);
            return timestamp != null ? timestamp.toInstant() : null;
        }
    }
}