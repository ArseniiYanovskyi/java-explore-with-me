package ru.practicum.dao;

import ru.practicum.dto.StatisticAnswerDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EndpointHitRepository {

    void saveStatisticInfo(EndpointHit endpointHit);

    List<StatisticAnswerDto> getStatistic(List<String> uris, LocalDateTime start, LocalDateTime end);

    List<StatisticAnswerDto> getUniqueIpStatistic(List<String> uris, LocalDateTime start, LocalDateTime end);

    Optional<StatisticAnswerDto> getStatisticByEndpoint(String url);
}
