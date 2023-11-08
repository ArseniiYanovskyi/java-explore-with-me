package ru.practicum.request.service;

import ru.practicum.request.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.model.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto privateAddNewRequest(long userId, long eventId);

    EventRequestStatusUpdateResult privateUpdateOwnEventRequests(long userId, long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    ParticipationRequestDto privateCancelOwnRequest(long userId, long requestId);

    List<ParticipationRequestDto> privateGetOwnEventRequests(long userId, long eventId);

    List<ParticipationRequestDto> privateGetOwnRequests(long userId);

}
