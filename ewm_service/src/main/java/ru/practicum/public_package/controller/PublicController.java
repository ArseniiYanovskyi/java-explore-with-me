package ru.practicum.public_package.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatisticClient;
import ru.practicum.dto.StatisticInfoDto;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.EventShortDto;
import ru.practicum.model.event.dto.PublicSearchEventParameters;
import ru.practicum.public_package.service.PublicService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PublicController {
    private final PublicService publicService;
    private final StatisticClient statisticClient;

    @GetMapping("/categories")
    @ResponseStatus(code = HttpStatus.OK)
    public List<CategoryDto> getCategoriesList(@RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
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
    @ResponseStatus(code = HttpStatus.OK)
    public List<CompilationDto> getCompilationList(@RequestParam(required = false) Boolean pinned, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get compilations.");
        return publicService.getCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CompilationDto getCompilationListById(@PathVariable long compId) {
        log.info("Received request to get compilation with id: {}.", compId);
        return publicService.getCompilationById(compId);
    }

    @GetMapping("/events")
    @ResponseStatus(code = HttpStatus.OK)
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories, @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) String rangeStart, @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable, @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                         @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size,
                                         HttpServletRequest request) {
        log.info("Receive request to get events by parameters.");
        List<EventShortDto> result = publicService.getEventsByParameters(PublicSearchEventParameters.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sortType(sort)
                .from(from)
                .size(size)
                .build());
        sendStatistic(request);
        return result;
    }

    @GetMapping("/events/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public EventFullDto getEventById(@PathVariable long eventId, HttpServletRequest request) {
        log.info("Receive request to get events by id {}.", request);
        EventFullDto result = publicService.getEventById(eventId);
        sendStatistic(request);
        return result;
    }

    private void sendStatistic(HttpServletRequest request) {
        log.info("Sending to statistic client information about request. Api: explore_with_me_service, URI: {}, IP: {}.",
                request.getRequestURI(), request.getRemoteAddr());
        statisticClient.post(StatisticInfoDto.builder()
                .app("explore_with_me_service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(Mapper.formatter))
                .build());
    }
}
