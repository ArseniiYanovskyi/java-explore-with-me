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
    public EventFullDto addNewEvent(long userId, NewEventDto newEventDto);
    public ParticipationRequestDto addNewEventRequest(long userId, long eventId);
    public EventFullDto updateEventByOwner(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);
    public EventRequestStatusUpdateResult updateEventRequests(long userId, long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
    public EventFullDto getEventInformation(long userId, long eventId);
    public List<EventShortDto> getUserEvents(long userId, int from, int size);
    public List<ParticipationRequestDto> getEventRequests(long userId, long eventId);
    public List<ParticipationRequestDto> getOwnRequestsInformation(long userId);
    public ParticipationRequestDto deleteOwnRequest(long userId, long requestId);
}
