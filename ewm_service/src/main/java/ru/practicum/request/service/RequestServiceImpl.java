package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.exception.model.ConflictRequestException;
import ru.practicum.exception.model.NotFoundException;
import ru.practicum.mapper.Mapper;
import ru.practicum.request.dao.RequestRepository;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.Status;
import ru.practicum.request.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.model.dto.ParticipationRequestDto;
import ru.practicum.serviceutils.ServiceUtils;
import ru.practicum.users.dao.UserRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final ServiceUtils utils;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public ParticipationRequestDto privateAddNewRequest(long userId, long eventId) {
        Request request = createRequest(userId, eventId);

        checkRequestConflict(request, requestRepository.findByRequesterIdAndEventId(userId, eventId));

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
        ParticipationRequestDto returningDto = Mapper.createParticipationRequestDto(requestRepository.save(request));
        if (isUpdateConformingRequestsRequire) {
            updateConfirmedRequests(request.getEvent(), 1);
        }
        return returningDto;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult privateUpdateOwnEventRequests(long userId, long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " from user " + userId + " does not present in repository."));

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
        EventRequestStatusUpdateResult requestStatusUpdateResult = createRequestsResultDto(requestRepository.findAllByEventId(eventId));

        updateConfirmedRequests(event, requestStatusUpdateResult.getConfirmedRequests().size());

        return requestStatusUpdateResult;
    }

    @Override
    @Transactional
    public ParticipationRequestDto privateCancelOwnRequest(long userId, long requestId) {
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id " + requestId + " from user " + userId + " does not present in repository."));
        request.setStatus(Status.CANCELED);
        log.info("Sending to repository request to update request {} information to canceled.", requestId);
        return Mapper.createParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> privateGetOwnEventRequests(long userId, long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " from user " + userId + " does not present in repository."));
        log.info("Sending to repository request to get event {} requests from event owner.", event.getId());
        return requestRepository.findAllByEventId(event.getId()).stream()
                .map(Mapper::createParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> privateGetOwnRequests(long userId) {
        log.info("Sending to repository request to get information about user {} requests.", userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(Mapper::createParticipationRequestDto)
                .collect(Collectors.toList());
    }

    private Request createRequest(long requesterId, long eventId) {
        return Mapper.createRequest(userRepository.findById(requesterId)
                        .orElseThrow(() -> new NotFoundException("User with id " + requesterId + " does not present in repository.")),
                eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("User with id " + eventId + " does not present in repository.")));
    }

    public void checkRequestConflict(Request request, Optional<Request> optionalRequest) {
        if (optionalRequest.isPresent()) {
            throw new ConflictRequestException("User already send request for this event.");
        }
        if (request.getEvent().getInitiator().getId() == request.getRequester().getId()) {
            throw new ConflictRequestException("User can't send request for own event.");
        }
        if (request.getEvent().getParticipantLimit() != 0 && (request.getEvent().getParticipantLimit() <= request.getEvent().getConfirmedRequests())) {
            throw new ConflictRequestException("Participation limit exceeded.");
        }
        if (!request.getEvent().getState().equals(State.PUBLISHED)) {
            throw new ConflictRequestException("Event " + request.getEvent().getId() + " is not published.");
        }
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

    private EventRequestStatusUpdateResult createRequestsResultDto(List<Request> requestList) {
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();
        for (Request request : requestList) {
            if (request.getStatus().equals(Status.CONFIRMED)) {
                confirmed.add(Mapper.createParticipationRequestDto(request));
            } else if (request.getStatus().equals(Status.REJECTED)) {
                rejected.add(Mapper.createParticipationRequestDto(request));
            }
        }
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }
}
