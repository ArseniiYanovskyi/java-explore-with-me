package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.StatisticClient;
import ru.practicum.dto.StatisticInfoDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ExploreWithMeController {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatisticClient statisticClient;

    @GetMapping("/events")
    public void getEvents(HttpServletRequest request) {
        log.info("Sending to statistic client information about request. Api: explore_with_me_service, URI: {}, IP: {}.",
                request.getRequestURI(), request.getRemoteAddr());
        statisticClient.post(StatisticInfoDto.builder()
                .app("explore_with_me_service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(formatter))
                .build());
    }

    @GetMapping("/events/{eventId}")
    public void getEventById(@PathVariable long eventId, HttpServletRequest request) {
        log.info("Sending to statistic client information about request. Api: explore_with_me_service, URI: {}, IP: {}.",
                request.getRequestURI(), request.getRemoteAddr());
        statisticClient.post(StatisticInfoDto.builder()
                .app("explore_with_me_service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(formatter))
                .build());
    }
}
