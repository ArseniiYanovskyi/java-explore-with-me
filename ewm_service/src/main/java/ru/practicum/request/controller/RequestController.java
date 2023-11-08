package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.model.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    private final RequestService requestService;

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ParticipationRequestDto privateAddNewRequest(@PathVariable long userId, @RequestParam long eventId) {
        log.info("Received request to add new participation request for event {}  from user: {}.", eventId, userId);
        return requestService.privateAddNewRequest(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(code = HttpStatus.OK)
    public EventRequestStatusUpdateResult privateUpdateEventRequests(@PathVariable long userId, @PathVariable long eventId,
                                                                     @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Received request to update event: {} participants.", eventId);
        return requestService.privateUpdateOwnEventRequests(userId, eventId, eventRequestStatusUpdateRequest);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(code = HttpStatus.OK)
    public ParticipationRequestDto privateDeleteRequest(@PathVariable long userId, @PathVariable long requestId) {
        log.info("Received request to delete request: {} from user {}.", requestId, userId);
        return requestService.privateCancelOwnRequest(userId, requestId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ParticipationRequestDto> privateGetEventRequests(@PathVariable long userId, @PathVariable long eventId) {
        log.info("Received request to get information about event: {} participants, from user: {}.", eventId, userId);
        return requestService.privateGetOwnEventRequests(userId, eventId);
    }

    @GetMapping("/users/{userId}/requests")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ParticipationRequestDto> getOwnRequestsInformation(@PathVariable long userId) {
        log.info("Received request to get information about own requests from user: {}.", userId);
        return requestService.privateGetOwnRequests(userId);
    }
}
