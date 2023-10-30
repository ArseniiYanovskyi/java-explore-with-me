package ru.practicum.private_package.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.NewEventDto;
import ru.practicum.model.event.dto.UpdateEventUserRequest;
import ru.practicum.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.model.request.dto.ParticipationRequestDto;
import ru.practicum.private_package.service.PrivateService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PrivateController {
    private final PrivateService privateService;
    @PostMapping("/users/{userId}/events")
    @ResponseStatus(code = HttpStatus.CREATED)
    public EventFullDto addNewEvent(@PathVariable long userId, @RequestBody NewEventDto newEventDto) {
        log.info("Received request to add new event from user: {}.", userId);
        return privateService.addNewEvent(userId, newEventDto);
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ParticipationRequestDto addNewRequest(@PathVariable long userId, @RequestParam long eventId) {
        log.info("Received request to add new participation request for event {}  from user: {}.", eventId, userId);
        return null;
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public EventFullDto updateEventByOwner(@PathVariable long userId, @PathVariable long eventId,
                                           @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Received request to update event: {}, from user: {}.", eventId, userId);
        return privateService.updateEventByOwner(userId, eventId, updateEventUserRequest);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(code = HttpStatus.OK)
    public EventRequestStatusUpdateResult updateEventRequests(@PathVariable long userId, @PathVariable long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Received request to update event: {} participants.", eventId);
        return privateService.updateEventRequests(userId, eventId, eventRequestStatusUpdateRequest);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public EventFullDto getEventInformation(@PathVariable long userId, @PathVariable long eventId) {
        log.info("Received request to get information about event: {}, from user: {}.", eventId, userId);
        return privateService.getEventInformation(userId, eventId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ParticipationRequestDto> getEventRequests(@PathVariable long userId, @PathVariable long eventId) {
        log.info("Received request to get information about event: {} participants, from user: {}.", eventId, userId);
        return privateService.getEventRequests(userId, eventId);
    }
}
