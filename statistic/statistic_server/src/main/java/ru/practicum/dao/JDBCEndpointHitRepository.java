package ru.practicum.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.StatisticAnswerDto;
import ru.practicum.model.EndpointHit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JDBCEndpointHitRepository implements EndpointHitRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveStatisticInfo(EndpointHit endpointHit) {
        final String sqlQuery = "INSERT INTO endpointhits (app, uri, ip, timestamp) "
                + "VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, endpointHit.getApp());
            stmt.setString(2, endpointHit.getUri());
            stmt.setString(3, endpointHit.getIp());
            stmt.setTimestamp(4, Timestamp.valueOf(endpointHit.getHitTime()));
            return stmt;
        }, keyHolder);
    }

    @Override
    public List<StatisticAnswerDto> getStatistic(List<String> uris, LocalDateTime start, LocalDateTime end) {
        if (uris.isEmpty()) {
            String sqlQueryWithoutUris = "SELECT app, uri, COUNT(ip) AS hits " +
                    "FROM endpointhits " +
                    "WHERE timestamp > ? AND timestamp < ? " +
                    "GROUP BY uri, app " +
                    "ORDER BY COUNT(ip) DESC";
            return jdbcTemplate.query(sqlQueryWithoutUris, (rs, rowNum) -> makeAnswer(rs),
                    Timestamp.valueOf(start), Timestamp.valueOf(end));
        }
        List<StatisticAnswerDto> returningList = new ArrayList<>();
        final String sqlQuery = "SELECT app, uri, COUNT(ip) AS hits " +
                "FROM endpointhits " +
                "WHERE uri LIKE ? AND timestamp > ? AND timestamp < ? " +
                "GROUP BY uri, app " +
                "ORDER BY COUNT(ip) DESC";
        for (String uri : uris) {
            returningList.addAll(jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeAnswer(rs), uri,
                    Timestamp.valueOf(start), Timestamp.valueOf(end)));
        }

        return returningList.stream()
                .sorted(Comparator.comparingInt(StatisticAnswerDto::getHits))
                .collect(Collectors.toList());
    }

    @Override    public List<StatisticAnswerDto> getUniqueIpStatistic(List<String> uris, LocalDateTime start, LocalDateTime end) {
        if (uris.isEmpty()) {
            String sqlQueryWithoutUris = "SELECT app, uri, COUNT(DISTINCT(ip)) AS hits " +
                    "FROM endpointhits " +
                    "WHERE timestamp > ? AND timestamp < ? " +
                    "GROUP BY uri, app " +
                    "ORDER BY COUNT(DISTINCT(ip)) DESC";
            return jdbcTemplate.query(sqlQueryWithoutUris, (rs, rowNum) -> makeAnswer(rs),
                    Timestamp.valueOf(start), Timestamp.valueOf(end));
        }
        List<StatisticAnswerDto> returningList = new ArrayList<>();
        final String sqlQueryByUris = "SELECT app, uri, COUNT(DISTINCT(ip)) AS hits " +
                "FROM endpointhits " +
                "WHERE uri LIKE ? AND timestamp > ? AND timestamp < ? " +
                "GROUP BY uri, app " +
                "ORDER BY COUNT(DISTINCT(ip)) DESC";

        for (String uri : uris) {
            returningList.addAll(jdbcTemplate.query(sqlQueryByUris, (rs, rowNum) -> makeAnswer(rs), uri,
                    Timestamp.valueOf(start), Timestamp.valueOf(end)));
        }

        return returningList.stream()
                .sorted(Comparator.comparingInt(StatisticAnswerDto::getHits))
                .collect(Collectors.toList());
    }

    private StatisticAnswerDto makeAnswer(ResultSet rs) {
        try {
            return StatisticAnswerDto.builder()
                    .app(rs.getString("app"))
                    .uri(rs.getString("uri"))
                    .hits(rs.getInt("hits"))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException("Error getting statistic information.");
        }
    }
}
