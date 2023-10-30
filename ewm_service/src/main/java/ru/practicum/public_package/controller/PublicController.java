package ru.practicum.public_package.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatisticClient;
import ru.practicum.dto.StatisticInfoDto;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.public_package.service.PublicService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PublicController {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final PublicService publicService;
    private final StatisticClient statisticClient;

    @GetMapping("/categories")
    @ResponseStatus(code = HttpStatus.OK)
    public List<CategoryDto> getCategoriesList(@RequestParam(defaultValue = "0") int from,
                                               @RequestParam (defaultValue = "10") int size) {
        log.info("Received request to get categories list from: {}, size: {}.", from, size);
        return publicService.getCategoriesList(from, size);
    }

    @GetMapping("/categories/{catId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CategoryDto getCategoryById(@PathVariable long catId) {
        log.info("Received request to get category with id: {}.", catId);
        return publicService.getCategoryById(catId);
    }

    @GetMapping("/compilations")
    public void getCompilationList() {

    }

    @GetMapping("/compilations/{compId}")
    public void getCompilationListById() {

    }

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
