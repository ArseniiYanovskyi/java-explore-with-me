package ru.practicum.private_package.service.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dao.CategoryRepository;
import ru.practicum.dao.EventRepository;
import ru.practicum.dao.UserRepository;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.NewEventDto;
import ru.practicum.model.event.dto.StateAction;
import ru.practicum.model.event.dto.UpdateEventUserRequest;
import ru.practicum.model.exception.IncorrectRequestException;
import ru.practicum.model.exception.NotFoundException;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.Status;
import ru.practicum.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.model.request.dto.ParticipationRequestDto;
import ru.practicum.model.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PrivateServiceUtils {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public Event convertEventFromNewDto(NewEventDto newEventDto, long userId) {
        if (LocalDateTime.parse(newEventDto.getEventDate(), Mapper.formatter).isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IncorrectRequestException("Time of new event can't be earlier than 2 hours later.");
        }
        Category eventCategory = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id " + newEventDto.getCategory() + " does not present in repository."));
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " does not present in repository."));
        return Mapper.convertNewEventFromDto(newEventDto, initiator, eventCategory);
    }

    public EventFullDto convertEventToFullDto(Event event) {
        return Mapper.convertEventToFullDto(event);
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
        if (LocalDateTime.parse(updateEventUserRequest.getEventDate(), Mapper.formatter).isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IncorrectRequestException("Time of updated by owner event can't be earlier than 2 hours later.");
        }
        if (updateEventUserRequest.getCategory() != null && updateEventUserRequest.getCategory() != 0) {
            Category newCategory = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id " + updateEventUserRequest.getCategory() + " does not present in repository."));
            event.setCategory(newCategory);
        }
        if(updateEventUserRequest.getAnnotation() != null && !updateEventUserRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getDescription() != null && !updateEventUserRequest.getDescription().isBlank()) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getTitle() != null && !updateEventUserRequest.getTitle().isBlank()) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        if(updateEventUserRequest.getEventDate() != null && !updateEventUserRequest.getEventDate().isBlank()) {
            event.setEventDate(LocalDateTime.parse(updateEventUserRequest.getEventDate(), Mapper.formatter));
        }
        if (updateEventUserRequest.getLocation() != null && updateEventUserRequest.getLocation().getLat() != 0 && updateEventUserRequest.getLocation().getLen() != 0) {
            event.setLatitude(updateEventUserRequest.getLocation().getLat());
            event.setLongitude(updateEventUserRequest.getLocation().getLen());
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
        if (event.getState().equals(State.PENDING) && updateEventUserRequest.getStateAction().equals(StateAction.CANCEL_REVIEW)) {
            event.setState(State.CANCELED);
        } else if (event.getState().equals(State.CANCELED) && updateEventUserRequest.getStateAction().equals(StateAction.SEND_TO_REVIEW)) {
            event.setState(State.PENDING);
        }
        return event;
    }

    public Request createRequest(long requesterId, long eventId) {
        return Mapper.createRequest(userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("User with id " + requesterId + " does not present in repository.")),
                eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("User with id " + eventId + " does not present in repository.")));
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

    public void checkIsUserPresent(long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User with id " + userId + " does not present in repository.");
        }
    }
}
