package ru.practicum.private_package.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dao.EventRepository;
import ru.practicum.dao.RequestRepository;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.EventShortDto;
import ru.practicum.model.event.dto.NewEventDto;
import ru.practicum.model.event.dto.UpdateEventUserRequest;
import ru.practicum.model.exception.ConflictRequestException;
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
        utils.checkNewEventDtoValidation(newEventDto);
        Event event = utils.convertEventFromNewDto(newEventDto, userId);
        log.info("Sending to repository request to add new event with status: {}.", event.getState());
        return utils.convertEventToFullDto(eventRepository.save(event));
    }

    @Override
    public ParticipationRequestDto addNewEventRequest(long userId, long eventId) {
        Request request = utils.createRequest(userId, eventId);

        String newRequestConflictCheckResult = utils.checkRequestConflict(request, requestRepository.findByRequesterIdAndEventId(userId, eventId));
        if (!newRequestConflictCheckResult.equals("ok")) {
            log.info("New event request conflict check has failed, return exception ({}).", newRequestConflictCheckResult);
            throw new ConflictRequestException(newRequestConflictCheckResult);
        }

        boolean isUpdateConformingRequestsRequire = false;
        if (!request.getEvent().isRequestModeration()) {
            log.info("Event " + eventId + " does not require moderation, request confirmed automatically.");
            request.setStatus(Status.CONFIRMED);
            isUpdateConformingRequestsRequire = true;
        }
        if (request.getEvent().getParticipantLimit() == 0) {
            log.info("Event " + eventId + " does not have participants limit, request confirmed automatically.");
            request.setStatus(Status.CONFIRMED);
            isUpdateConformingRequestsRequire = true;
        }
        log.info("Sending to repository request to add new request for event {} from user {}.", eventId, userId);
        ParticipationRequestDto returningDto = utils.createRequestDto(requestRepository.save(request));
        if (isUpdateConformingRequestsRequire) {
            updateConfirmedRequests(request.getEvent(), 1);
        }
        return returningDto;
    }

    @Override
    public EventFullDto updateEventByOwner(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = utils.getEventByOwner(userId, eventId);
        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictRequestException("Event already published, owner can't edit it anymore.");
        }
        event = utils.updateEventObject(event, updateEventUserRequest);
        log.info("Sending to repository request to update event with id {} by owner.", eventId);
        return utils.convertEventToFullDto(eventRepository.save(event));
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequests(long userId, long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        Event event = utils.getEventByOwner(userId, eventId);

        List<Request> requests = new ArrayList<>();
        for (Long requesterId : eventRequestStatusUpdateRequest.getRequestIds()) {
            Request request = requestRepository.findByIdAndEventId(requesterId, eventId)
                    .orElseThrow(() -> new NotFoundException("Request " + requesterId + " for event " + eventId + " does not present in repository."));
            if (!request.getStatus().equals(Status.PENDING)) {
                throw new ConflictRequestException("Request" + request.getId() + " status is already " + request.getStatus() + ".");
            }
            if (eventRequestStatusUpdateRequest.getStatus().equals(Status.CONFIRMED)) {
                request.setStatus(Status.CONFIRMED);
            } else if (eventRequestStatusUpdateRequest.getStatus().equals(Status.REJECTED)) {
                request.setStatus(Status.REJECTED);
            }
            requests.add(request);
        }
        if (eventRequestStatusUpdateRequest.getStatus().equals(Status.CONFIRMED) && ((event.getParticipantLimit() - event.getConfirmedRequests()) < requests.size())) {
            throw new ConflictRequestException("Event limit for participants exceeded.");
        }
        log.info("Sending to repository request to set event status {} for users {}.", eventRequestStatusUpdateRequest.getStatus(), eventRequestStatusUpdateRequest.getRequestIds());
        requestRepository.saveAll(requests);
        EventRequestStatusUpdateResult requestStatusUpdateResult = utils.createRequestsResultDto(requestRepository.findAllByEventId(eventId));

        updateConfirmedRequests(event, requestStatusUpdateResult.getConfirmedRequests().size());

        return requestStatusUpdateResult;
    }

    @Override
    public EventFullDto getEventInformation(long userId, long eventId) {
        log.info("Sending to repository request to get event with id {} by user {}.", eventId, userId);
        return utils.convertEventToFullDto(utils.getEventByOwner(userId, eventId));
    }

    @Override
    public List<EventShortDto> getUserEvents(long userId, int from, int size) {
        log.info("Sending to repository request to get events of user: {}.", userId);
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(utils::convertEventToShortDto)
                .collect(Collectors.toList());
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
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id " + requestId + " from user " + userId + " does not present in repository."));
        request.setStatus(Status.CANCELED);
        log.info("Sending to repository request to update request {} information to canceled.", requestId);
        return utils.createRequestDto(requestRepository.save(request));
    }

    private void updateConfirmedRequests(Event event, int amountToAdd) {
        event.setConfirmedRequests(amountToAdd);
        log.info("Sending to repository request to update confirmed requests for event {}.", event.getId());
        eventRepository.save(event);

        if (event.getParticipantLimit() == event.getConfirmedRequests()) {
            List<Request> otherEventRequests = requestRepository.findAllByEventIdAndStatus(event.getId(), Status.PENDING);
            if (otherEventRequests == null || otherEventRequests.isEmpty()) {
                return;
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
    }
}
