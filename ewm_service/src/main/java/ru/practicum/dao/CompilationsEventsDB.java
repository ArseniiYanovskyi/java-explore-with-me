package ru.practicum.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CompilationsEventsDB {
    private final JdbcTemplate jdbcTemplate;

    public void saveCompilationEvents(long compilationId, List<Long> eventsId) {
        final String sqlQuery = "INSERT INTO compilations_events (compilation_id, event_id) " +
                "values (?, ?)";
        for (Long eventId : eventsId) {
            jdbcTemplate.update(sqlQuery, compilationId, eventId);
        }
    }

    public List<Long> getCompilationEvents(long compilationId) {
        final String sqlQuery = "SELECT event_id FROM compilations_events " +
                "WHERE compilation_id = ?";
        return jdbcTemplate.query(sqlQuery, new IdRowMapper(), compilationId);
    }

    public void deleteCompilationEvents(long compilationId) {
        final String sqlQuery = "DELETE FROM compilations_events WHERE compilation_id = ?";
        jdbcTemplate.update(sqlQuery, compilationId);
    }

    private static class IdRowMapper implements RowMapper<Long> {
        @Override
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getLong("event_id");
        }
    }
}
