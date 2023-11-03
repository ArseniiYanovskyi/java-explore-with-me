package ru.practicum.private_package.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.EventShortDto;
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
        return privateService.addNewEventRequest(userId, eventId);
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

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(code = HttpStatus.OK)
    public ParticipationRequestDto deleteOwnRequest(@PathVariable long userId, @PathVariable long requestId) {
        log.info("Received request to delete request: {} from user {}.", requestId, userId);
        return privateService.deleteOwnRequest(userId, requestId);
    }

    @GetMapping("/users/{userId}/events")
    @ResponseStatus(code = HttpStatus.OK)
    public List<EventShortDto> getUserEvents(@PathVariable long userId, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get events of user: {}.", userId);
        return privateService.getUserEvents(userId, from, size);
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
    @GetMapping("/users/{userId}/requests")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ParticipationRequestDto> getOwnRequestsInformation(@PathVariable long userId) {
        log.info("Received request to get information about own requests from user: {}.", userId);
        return privateService.getOwnRequestsInformation(userId);
    }
}
