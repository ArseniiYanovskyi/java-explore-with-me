package ru.practicum.mapper;

import ru.practicum.dto.StatisticInfoDto;
import ru.practicum.dto.StatisticRequestDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StatisticMapper {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHit convertToEndpointHit(StatisticInfoDto statisticInfoDto) {
        return EndpointHit.builder()
                .app(statisticInfoDto.getApp())
                .uri(statisticInfoDto.getUri())
                .ip(statisticInfoDto.getIp())
                .hitTime(LocalDateTime.parse(statisticInfoDto.getTimestamp(), formatter))
                .build();
    }

    public static StatisticRequestDto createStatisticRequestDto(String start, String end, List<String> uris, Boolean unique) {
        if (uris == null) {
            uris = new ArrayList<>();
        }
        return StatisticRequestDto.builder()
                .start(LocalDateTime.parse(start, formatter))
                .end(LocalDateTime.parse(end, formatter))
                .uris(uris)
                .unique(unique)
                .build();
    }
}
