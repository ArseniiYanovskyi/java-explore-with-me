package ru.practicum.event.service;

import ru.practicum.event.model.dto.*;

import java.util.List;

public interface EventService {
    EventFullDto privateAddNewEvent(long userId, NewEventDto newEventDto);

    EventFullDto adminUpdateEvent(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDto privateUpdateEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventFullDto> adminSearchEvent(AdminEventSearchParameters adminEventSearchParameters);

    List<EventShortDto> privateGetUserEvents(long userId, int from, int size);

    EventFullDto privateGetEventById(long userId, long eventId);

    List<EventShortDto> publicSearchEvents(PublicSearchEventParameters parameters);

    EventFullDto publicGetEventById(long eventId);
}
