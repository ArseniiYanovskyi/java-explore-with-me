package ru.practicum.private_package.service;

import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.EventShortDto;
import ru.practicum.model.event.dto.NewEventDto;
import ru.practicum.model.event.dto.UpdateEventUserRequest;
import ru.practicum.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.model.request.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateService {
    EventFullDto addNewEvent(long userId, NewEventDto newEventDto);

    ParticipationRequestDto addNewEventRequest(long userId, long eventId);

    EventFullDto updateEventByOwner(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventRequestStatusUpdateResult updateEventRequests(long userId, long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    EventFullDto getEventInformation(long userId, long eventId);

    List<EventShortDto> getUserEvents(long userId, int from, int size);

    List<ParticipationRequestDto> getEventRequests(long userId, long eventId);

    List<ParticipationRequestDto> getOwnRequestsInformation(long userId);

    ParticipationRequestDto deleteOwnRequest(long userId, long requestId);
}
