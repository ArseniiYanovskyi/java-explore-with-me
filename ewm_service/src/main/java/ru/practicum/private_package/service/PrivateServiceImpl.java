package ru.practicum.private_package.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dao.EventRepository;
import ru.practicum.dao.RequestRepository;
import ru.practicum.dao.UserRepository;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.NewEventDto;
import ru.practicum.model.event.dto.UpdateEventUserRequest;
import ru.practicum.model.exception.IncorrectRequestException;
import ru.practicum.model.exception.NotFoundException;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.Status;
import ru.practicum.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.model.request.dto.ParticipationRequestDto;
import ru.practicum.private_package.service.utils.PrivateServiceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivateServiceImpl implements PrivateService {
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
        Request request = utils.createRequest(userId, eventId);
        if (!request.getEvent().isPublished()) {
            throw new IncorrectRequestException("Event " + eventId + " is not published.");
        }
        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new IncorrectRequestException("Event " + eventId + " already has request from user " + userId + ".");
        }
        if (request.getEvent().getParticipantLimit() != 0 && request.getEvent().getParticipantLimit() == request.getEvent().getConfirmedRequests()) {
            throw new IncorrectRequestException("Participation limit for event " + eventId + " exceeded.");
        }
        if (request.getEvent().getInitiator().getId() == userId) {
            throw new IncorrectRequestException("Event initiator can't send request for participation in own event.");
        }
        if (!request.getEvent().isRequestModeration()) {
            log.info("Event "  + eventId + " does not require moderation, request confirmed automatically.");
            request.setStatus(Status.CONFIRMED);
        }
        log.info("Sending to repository request to add new request for event {} from user {}.", eventId, userId);
        return utils.createRequestDto(requestRepository.save(request));
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
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0 || event.getParticipantLimit() == event.getConfirmedRequests()) {
            return utils.createRequestsResultDto(requestRepository.findAllByEventId(eventId));
        }
        List<Request> requests = new ArrayList<>();
        for (Long requesterId : eventRequestStatusUpdateRequest.getRequestIds()) {
            Request request = requestRepository.findByRequesterIdAndEventId(requesterId, eventId)
                    .orElseThrow(() -> new NotFoundException("Request for event " + eventId + " from user with id " + userId + " does not present in repository."));
            if (!request.getStatus().equals(Status.PENDING)) {
                throw new IncorrectRequestException("Request" + request.getId() + " status is already " + request.getStatus() + ".");
            }
            requests.add(request);
        }
        if ((event.getParticipantLimit() - event.getConfirmedRequests()) < requests.size()) {
            throw new IncorrectRequestException("Event limit for participants exceeded.");
        }
        log.info("Sending to repository request to set event status {} for users {}.", eventRequestStatusUpdateRequest.getStatus(), eventRequestStatusUpdateRequest.getRequestIds());
        requestRepository.saveAll(requests);
        EventRequestStatusUpdateResult requestStatusUpdateResult = utils.createRequestsResultDto(requestRepository.findAllByEventId(eventId));
        event.setConfirmedRequests(requestStatusUpdateResult.getConfirmedRequests().size());
        log.info("Sending to repository request to update confirmed requests for event {}.", eventId);
        eventRepository.save(event);

        if (event.getParticipantLimit() == event.getConfirmedRequests()) {
            List<Request> otherEventRequests = requestRepository.findAllByEventIdAAndStatus(eventId, Status.PENDING);
            if (otherEventRequests == null || otherEventRequests.isEmpty()) {
                return requestStatusUpdateResult;
            }
            otherEventRequests = otherEventRequests.stream()
                    .map(request -> {
                        request.setStatus(Status.REJECTED);
                        return request;
                    })
                    .collect(Collectors.toList());
            log.info("Sending to repository request to update requests for event, which can't accept any other participants.");
            requestRepository.saveAll(otherEventRequests);
        }
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
        log.info("Sending to repository request to get event {} requests from event owner.", eventId);
        return utils.createRequestsResult(requestRepository.findAllByEventId(eventId));
    }

    @Override
    public List<ParticipationRequestDto> getOwnRequestsInformation(long userId) {
        utils.checkIsUserPresent(userId);
        log.info("Sending to repository request to get information about user {} requests.", userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(utils::createRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto deleteOwnRequest(long userId, long requestId) {
        utils.checkIsUserPresent(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id " + requestId + " does not present in repository."));
        request.setStatus(Status.REJECTED);
        log.info("Sending to repository request to update request {} information.", requestId);
        return utils.createRequestDto(requestRepository.save(request));
    }

}
