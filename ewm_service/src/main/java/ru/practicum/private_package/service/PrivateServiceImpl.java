package ru.practicum.private_package.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dao.EventRepository;
import ru.practicum.dao.RequestRepository;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.NewEventDto;
import ru.practicum.model.event.dto.UpdateEventUserRequest;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.model.request.dto.ParticipationRequestDto;
import ru.practicum.private_package.service.utils.PrivateServiceUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivateServiceImpl implements PrivateService{
    private final PrivateServiceUtils utils;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    @Override
    public EventFullDto addNewEvent(long userId, NewEventDto newEventDto) {
        Event event = utils.convertEventFromNewDto(newEventDto, userId);
        log.info("Sending to repository request to add new event with status: {}.", event.getState());
        return utils.convertEventToFullDto(eventRepository.save(event));
    }

    @Override
    public ParticipationRequestDto addNewEventRequest(long userId, long eventId) {
        return null;
    }

    @Override
    public EventFullDto updateEventByOwner(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = utils.getEventByOwner(userId, eventId);
        event = utils.updateEventObject(event, updateEventUserRequest);
        log.info("Sending to repository request to update event with id {} by owner.", eventId);
        return utils.convertEventToFullDto(eventRepository.save(event));
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequests(long userId, long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        Event event = utils.getEventByOwner(userId, eventId);
        List<Request> requests = new ArrayList<>();
        for (Long requesterId : eventRequestStatusUpdateRequest.getRequestIds()) {
            Request request = requestRepository.findByRequesterIdAndEventId(requesterId, eventId)
                    .orElse(utils.createRequest(requesterId, event, eventRequestStatusUpdateRequest.getStatus()));
            requests.add(request);
        }
        log.info("Sending to repository request to set event status {} for users {}.", eventRequestStatusUpdateRequest.getStatus(), eventRequestStatusUpdateRequest.getRequestIds());
        requestRepository.saveAll(requests);
        EventRequestStatusUpdateResult requestStatusUpdateResult = utils.createRequestsResultDto(requestRepository.findAllByEventId(eventId));
        event.setConfirmedRequests(requestStatusUpdateResult.getConfirmedRequests().size());
        log.info("Sending to repository request to update confirmed requests for event {}.", eventId);
        eventRepository.save(event);
        return requestStatusUpdateResult;
    }

    @Override
    public EventFullDto getEventInformation(long userId, long eventId) {
        log.info("Sending to repository request to get event with id {} by user {}.", eventId, userId);
        return utils.convertEventToFullDto(utils.getEventByOwner(userId, eventId));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(long userId, long eventId) {
        Event event = utils.getEventByOwner(userId, eventId);
        log.info("Sending to repository request to get event {} requests from owner.", eventId);
        return utils.createRequestsResult(requestRepository.findAllByEventId(eventId));
    }
}
