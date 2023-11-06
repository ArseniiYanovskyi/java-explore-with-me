package ru.practicum.private_package.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatisticClient;
import ru.practicum.dao.CategoryRepository;
import ru.practicum.dao.EventRepository;
import ru.practicum.dao.UserRepository;
import ru.practicum.dto.StatisticAnswerDto;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;
import ru.practicum.model.event.dto.*;
import ru.practicum.model.exception.NotFoundException;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.Status;
import ru.practicum.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.model.request.dto.ParticipationRequestDto;
import ru.practicum.model.user.User;
import ru.practicum.validation.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PrivateServiceUtils {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final StatisticClient statisticClient;
    private final ObjectMapper objectMapper;

    public Event convertEventFromNewDto(NewEventDto newEventDto, long userId) {
        Category eventCategory = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id " + newEventDto.getCategory() + " does not present in repository."));
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " does not present in repository."));
        if (newEventDto.getParticipantLimit() == null) {
            newEventDto.setParticipantLimit(0);
        }
        if (newEventDto.getPaid() == null) {
            newEventDto.setPaid(false);
        }
        if (newEventDto.getRequestModeration() == null) {
            newEventDto.setRequestModeration(true);
        }
        return Mapper.convertNewEventFromDto(newEventDto, initiator, eventCategory);
    }

    public EventFullDto convertEventToFullDto(Event event) {
        return Mapper.convertEventToFullDto(event, getUniqueViews(event.getId()));
    }

    public Event getEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " does not present in repository."));
    }

    public Event getEventByOwner(long userId, long eventId) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new NotFoundException("Event with id " + eventId + " does now belong to user id " + userId + ".");
        }
        return event;
    }

    public Event updateEventObject(Event event, UpdateEventUserRequest updateEventUserRequest) {
        if (updateEventUserRequest.getEventDate() != null) {
            Validator.checkEventDatePrivate(updateEventUserRequest.getEventDate());
            event.setEventDate(LocalDateTime.parse(updateEventUserRequest.getEventDate(), Mapper.formatter));
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category newCategory = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id " + updateEventUserRequest.getCategory() + " does not present in repository."));
            event.setCategory(newCategory);
        }
        if (updateEventUserRequest.getAnnotation() != null && !updateEventUserRequest.getAnnotation().isBlank()) {
            Validator.checkAnnotation(updateEventUserRequest.getAnnotation());

            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getDescription() != null && !updateEventUserRequest.getDescription().isBlank()) {
            Validator.checkDescription(updateEventUserRequest.getDescription());

            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getTitle() != null && !updateEventUserRequest.getTitle().isBlank()) {
            Validator.checkTitle(updateEventUserRequest.getTitle());

            event.setTitle(updateEventUserRequest.getTitle());
        }
        if (updateEventUserRequest.getLocation() != null && updateEventUserRequest.getLocation().getLat() != 0 && updateEventUserRequest.getLocation().getLon() != 0) {
            event.setLatitude(updateEventUserRequest.getLocation().getLat());
            event.setLongitude(updateEventUserRequest.getLocation().getLon());
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null && updateEventUserRequest.getParticipantLimit() != 0) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            if (event.getState().equals(State.PENDING) && updateEventUserRequest.getStateAction().equals(StateAction.CANCEL_REVIEW)) {
                event.setState(State.CANCELED);
            } else if (event.getState().equals(State.CANCELED) && updateEventUserRequest.getStateAction().equals(StateAction.SEND_TO_REVIEW)) {
                event.setState(State.PENDING);
            }
        }
        return event;
    }

    public Request createRequest(long requesterId, long eventId) {
        return Mapper.createRequest(userRepository.findById(requesterId)
                        .orElseThrow(() -> new NotFoundException("User with id " + requesterId + " does not present in repository.")),
                eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("User with id " + eventId + " does not present in repository.")));
    }

    public ParticipationRequestDto createRequestDto(Request request) {
        return Mapper.createParticipationRequestDto(request);
    }

    public EventRequestStatusUpdateResult createRequestsResultDto(List<Request> requestList) {
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

    public List<ParticipationRequestDto> createRequestsResult(List<Request> requestList) {
        return requestList.stream()
                .map(Mapper::createParticipationRequestDto)
                .collect(Collectors.toList());
    }

    public EventShortDto convertEventToShortDto(Event event) {
        return Mapper.convertEventToShortDto(event, getUniqueViews(event.getId()));
    }


    public void checkIsUserPresent(long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User with id " + userId + " does not present in repository.");
        }
    }

    public void checkNewEventDtoValidation(NewEventDto newEventDto) {
        Validator.checkEventDatePrivate(newEventDto.getEventDate());
        Validator.checkTitle(newEventDto.getTitle());
        Validator.checkDescription(newEventDto.getDescription());
        Validator.checkAnnotation(newEventDto.getAnnotation());
        Validator.checkLocation(newEventDto.getLocation());
        Validator.checkCategory(newEventDto.getCategory());
    }

    public String checkRequestConflict(Request request, Optional<Request> optionalRequest) {
        if (optionalRequest.isPresent()) {
            return "User already send request for this event.";
        }
        if (request.getEvent().getInitiator().getId() == request.getRequester().getId()) {
            return "User can't send request for own event.";
        }
        if (request.getEvent().getParticipantLimit() != 0 && (request.getEvent().getParticipantLimit() <= request.getEvent().getConfirmedRequests())) {
            return "Participation limit exceeded.";
        }
        if (!request.getEvent().getState().equals(State.PUBLISHED)) {
            return "Event " + request.getEvent().getId() + " is not published.";
        }
        return "ok";
    }

    private long getUniqueViews(long eventId) {
        ResponseEntity<Object> statisticAnswer = statisticClient.getUniqueStatisticByEventId(eventId);
        try {
            StatisticAnswerDto result = objectMapper.convertValue(statisticAnswer.getBody(), StatisticAnswerDto.class);
            return result.getHits();
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }
}
