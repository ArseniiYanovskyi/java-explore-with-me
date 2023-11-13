package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatisticClient;
import ru.practicum.dto.StatisticInfoDto;
import ru.practicum.event.model.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.utils.Mapper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EventController {
    private final EventService eventService;

    private final StatisticClient statisticClient;

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(code = HttpStatus.CREATED)
    public EventFullDto privateAddNewEvent(@PathVariable long userId, @RequestBody @Valid NewEventDto newEventDto) {
        log.info("Received request to add new event from user: {}.", userId);
        return eventService.privateAddNewEvent(userId, newEventDto);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public EventFullDto privateUpdateEvent(@PathVariable long userId, @PathVariable long eventId,
                                           @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Received request to update event: {}, from user: {}.", eventId, userId);
        return eventService.privateUpdateEvent(userId, eventId, updateEventUserRequest);
    }

    @PatchMapping("/admin/events/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public EventFullDto adminUpdateEvent(@PathVariable long eventId, @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Received request to update event {}.", eventId);
        return eventService.adminUpdateEvent(eventId, updateEventAdminRequest);
    }

    @GetMapping("/users/{userId}/events")
    @ResponseStatus(code = HttpStatus.OK)
    public List<EventShortDto> privateGetUserEvents(@PathVariable long userId, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get events of user: {}.", userId);
        return eventService.privateGetUserEvents(userId, from, size);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public EventFullDto privateGetEventById(@PathVariable long userId, @PathVariable long eventId) {
        log.info("Received request to get information about event: {}, from user: {}.", eventId, userId);
        return eventService.privateGetEventById(userId, eventId);
    }

    @GetMapping("/admin/events")
    @ResponseStatus(code = HttpStatus.OK)
    public List<EventFullDto> adminSearchEvents(@RequestParam(required = false) List<Long> users,
                                                @RequestParam(required = false) List<String> states,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false) String rangeStart,
                                                @RequestParam(required = false) String rangeEnd,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to search events from users {} in {} states, in {} categories from time {} to time {}.",
                users, states, categories, rangeStart, rangeEnd);
        return eventService.adminSearchEvent(AdminEventSearchParameters.builder()
                .usersIds(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build());
    }

    @GetMapping("/events")
    @ResponseStatus(code = HttpStatus.OK)
    public List<EventShortDto> publicSearchEvents(@RequestParam(required = false) String text,
                                                  @RequestParam(required = false) List<Long> categories, @RequestParam(required = false) Boolean paid,
                                                  @RequestParam(required = false) String rangeStart, @RequestParam(required = false) String rangeEnd,
                                                  @RequestParam(defaultValue = "false") Boolean onlyAvailable, @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                  @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size,
                                                  HttpServletRequest request) {
        log.info("Receive request to get events by parameters.");
        List<EventShortDto> result = eventService.publicSearchEvents(PublicSearchEventParameters.builder()
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
    public EventFullDto publicGetEventById(@PathVariable long eventId, HttpServletRequest request) {
        log.info("Receive request to get events by id {}.", request);
        EventFullDto result = eventService.publicGetEventById(eventId);
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
