package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.category.model.Category;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.model.dto.*;
import ru.practicum.exception.model.BadRequestException;
import ru.practicum.exception.model.ConflictRequestException;
import ru.practicum.exception.model.NotFoundException;
import ru.practicum.mapper.Mapper;
import ru.practicum.serviceutils.ServiceUtils;
import ru.practicum.users.dao.UserRepository;
import ru.practicum.users.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ServiceUtils utils;

    @Override
    @Transactional
    public EventFullDto privateAddNewEvent(long userId, NewEventDto newEventDto) {
        if (LocalDateTime.parse(newEventDto.getEventDate(), Mapper.formatter).isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Private: Time of event can't be earlier than 2 hours later.");
        }

        Event event = convertEventFromNewDto(newEventDto, userId);
        log.info("Sending to repository request to add new event with status: {}.", event.getState());
        return utils.convertEventToFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventFullDto adminUpdateEvent(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event updatedEvent = adminUpdateEventObject(eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " does not present in repository.")),
                updateEventAdminRequest);
        log.info("Sending to repository request to update event {}.", eventId);
        return utils.convertEventToFullDto(eventRepository.save(updatedEvent));
    }

    @Override
    @Transactional
    public EventFullDto privateUpdateEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = utils.getEventWithOwnershipCheck(userId, eventId);
        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictRequestException("Event already published, owner can't edit it anymore.");
        }
        event = privateUpdateEvent(event, updateEventUserRequest);
        log.info("Sending to repository request to update event with id {} by owner.", eventId);
        return utils.convertEventToFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public List<EventFullDto> adminSearchEvent(AdminEventSearchParameters searchParameters) {
        List<Event> events = eventRepository.findAll();
        if (searchParameters.getUsersIds() != null && !searchParameters.getUsersIds().isEmpty()) {
            events = events.stream()
                    .filter(event -> searchParameters.getUsersIds().contains(event.getInitiator().getId()))
                    .collect(Collectors.toList());
        }
        if (searchParameters.getStates() != null && !searchParameters.getStates().isEmpty()) {
            List<State> states = searchParameters.getStates().stream()
                    .map(State::parseState)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            events = events.stream()
                    .filter(event -> states.contains(event.getState()))
                    .collect(Collectors.toList());
        }
        if (searchParameters.getCategories() != null && !searchParameters.getCategories().isEmpty()) {
            events = events.stream()
                    .filter(event -> searchParameters.getCategories().contains(event.getCategory().getId()))
                    .collect(Collectors.toList());
        }
        if (searchParameters.getRangeStart() != null) {
            LocalDateTime start = LocalDateTime.parse(searchParameters.getRangeStart(), Mapper.formatter);
            events = events.stream()
                    .filter(event -> event.getEventDate().isAfter(start))
                    .collect(Collectors.toList());
            if (searchParameters.getRangeEnd() != null) {
                LocalDateTime end = LocalDateTime.parse(searchParameters.getRangeEnd(), Mapper.formatter);
                events = events.stream()
                        .filter(event -> event.getEventDate().isBefore(end))
                        .collect(Collectors.toList());
            }
        }
        return formResultForAdminEventSearch(events, searchParameters.getFrom(), searchParameters.getSize());
    }

    @Override
    @Transactional
    public List<EventShortDto> privateGetUserEvents(long userId, int from, int size) {
        log.info("Sending to repository request to get events of user: {}.", userId);
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(utils::convertEventToShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto privateGetEventById(long userId, long eventId) {
        log.info("Sending to repository request to get event with id {} by user {}.", eventId, userId);
        return utils.convertEventToFullDto(utils.getEventWithOwnershipCheck(userId, eventId));
    }

    @Override
    @Transactional
    public List<EventShortDto> publicSearchEvents(PublicSearchEventParameters parameters) {
        List<Event> events = eventRepository.findAll();
        if (parameters.getText() != null && !parameters.getText().isBlank()) {
            events = events.stream()
                    .filter(event -> event.getDescription().toLowerCase().contains(parameters.getText().toLowerCase())
                            || event.getAnnotation().toLowerCase().contains(parameters.getText().toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (parameters.getCategories() != null && !parameters.getCategories().isEmpty()) {
            events = events.stream()
                    .filter(event -> parameters.getCategories().contains(event.getCategory().getId()))
                    .collect(Collectors.toList());
        }
        if (parameters.getPaid() != null) {
            events = events.stream()
                    .filter(event -> event.isPaid() == parameters.getPaid())
                    .collect(Collectors.toList());
        }
        if (parameters.getOnlyAvailable() != null) {
            events = events.stream()
                    .filter(event -> event.getParticipantLimit() == 0 || event.getConfirmedRequests() < event.getParticipantLimit())
                    .collect(Collectors.toList());
        }
        if (parameters.getRangeStart() != null) {
            LocalDateTime start = LocalDateTime.parse(parameters.getRangeStart(), Mapper.formatter);
            events = events.stream()
                    .filter(event -> event.getEventDate().isAfter(start))
                    .collect(Collectors.toList());
            if (parameters.getRangeEnd() != null) {
                LocalDateTime end = LocalDateTime.parse(parameters.getRangeEnd(), Mapper.formatter);
                if (start.isAfter(end)) {
                    throw new BadRequestException("End time can't be before start time.");
                }
                events = events.stream()
                        .filter(event -> event.getEventDate().isBefore(end))
                        .collect(Collectors.toList());
            }
        }

        return formResultForPublicEventSearch(events, parameters);
    }

    @Override
    @Transactional
    public EventFullDto publicGetEventById(long eventId) {
        log.info("Sending to repository request to get published event with id {}.", eventId);
        Event event = eventRepository.findByIdIsAndStateIs(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Published event with id " + eventId + " does not present in repository."));
        return utils.convertEventToFullDto(event);
    }

    private Event convertEventFromNewDto(NewEventDto newEventDto, long userId) {
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

    private void checkAnnotation(String annotation) {
        if (annotation == null) {
            throw new BadRequestException("Annotation absent.");
        }
        if (annotation.length() < 20 || annotation.length() > 2000) {
            throw new BadRequestException("Event annotation length should be between 20 and 2000 symbols.");
        }
    }

    private void checkDescription(String description) {
        if (description == null) {
            throw new BadRequestException("Description absent.");
        }
        if (description.length() < 20 || description.length() > 7000) {
            throw new BadRequestException("Event description length should be between 20 and 7000 symbols.");
        }
    }

    private void checkTitle(String title) {
        if (title == null) {
            throw new BadRequestException("Title absent.");
        }
        if (title.length() < 3 || title.length() > 120) {
            throw new BadRequestException("Event title length should be between 3 and 120 symbols.");
        }
    }

    private Event adminUpdateEventObject(Event event, UpdateEventAdminRequest updateEventAdminRequest) {
        if (updateEventAdminRequest.getEventDate() != null) {
            if (LocalDateTime.parse(updateEventAdminRequest.getEventDate(), Mapper.formatter).isBefore(LocalDateTime.now().plusHours(1))) {
                throw new BadRequestException("Admin: Time of event can't be earlier than 1 hours later.");
            }
            event.setEventDate(LocalDateTime.parse(updateEventAdminRequest.getEventDate(), Mapper.formatter));
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (!event.getState().equals(State.PENDING)) {
                throw new ConflictRequestException("Incorrect administrator state action.");
            }
            if (updateEventAdminRequest.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
                event.setState(State.PUBLISHED);
            }
            if (updateEventAdminRequest.getStateAction().equals(StateAction.REJECT_EVENT)) {
                event.setState(State.CANCELED);
            }
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Category newCategory = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id " + updateEventAdminRequest.getCategory() + " does not present in repository."));
            event.setCategory(newCategory);
        }
        if (updateEventAdminRequest.getAnnotation() != null && !updateEventAdminRequest.getAnnotation().isBlank()) {
            checkAnnotation(updateEventAdminRequest.getAnnotation());

            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getDescription() != null && !updateEventAdminRequest.getDescription().isBlank()) {
            checkDescription(updateEventAdminRequest.getDescription());

            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getTitle() != null && !updateEventAdminRequest.getTitle().isBlank()) {
            checkTitle(updateEventAdminRequest.getTitle());

            event.setTitle(updateEventAdminRequest.getTitle());
        }
        if (updateEventAdminRequest.getLocation() != null) {
            event.setLatitude(updateEventAdminRequest.getLocation().getLat());
            event.setLongitude(updateEventAdminRequest.getLocation().getLon());
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        return event;
    }

    private Event privateUpdateEvent(Event event, UpdateEventUserRequest updateEventUserRequest) {
        if (updateEventUserRequest.getEventDate() != null) {
            if (LocalDateTime.parse(updateEventUserRequest.getEventDate(), Mapper.formatter).isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Private: Time of event can't be earlier than 2 hours later.");
            }
            event.setEventDate(LocalDateTime.parse(updateEventUserRequest.getEventDate(), Mapper.formatter));
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category newCategory = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id " + updateEventUserRequest.getCategory() + " does not present in repository."));
            event.setCategory(newCategory);
        }
        if (updateEventUserRequest.getAnnotation() != null && !updateEventUserRequest.getAnnotation().isBlank()) {
            checkAnnotation(updateEventUserRequest.getAnnotation());

            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getDescription() != null && !updateEventUserRequest.getDescription().isBlank()) {
            checkDescription(updateEventUserRequest.getDescription());

            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getTitle() != null && !updateEventUserRequest.getTitle().isBlank()) {
            checkTitle(updateEventUserRequest.getTitle());

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

    private List<EventFullDto> formResultForAdminEventSearch(List<Event> events, int from, int size) {
        log.info("Repository answered {}, forming answer to controller.", events);
        if (events.size() < from) {
            log.info("Value 'from'({}) above result list size({}). Returning empty list.", from, events.size());
            return new ArrayList<>();
        }
        events = events.subList(from, events.size());
        if (events.size() > size) {
            events = events.subList(0, size);
        }
        return events.stream()
                .map(utils::convertEventToFullDto)
                .collect(Collectors.toList());
    }

    private List<EventShortDto> formResultForPublicEventSearch(List<Event> events, PublicSearchEventParameters parameters) {
        log.info("Repository answered {}, forming answer to controller.", events);
        PublicSearchEventSort sortType = PublicSearchEventSort.parseSearchSort(parameters.getSortType())
                .orElseThrow(() -> new NotFoundException("Such sort type can not be used is search."));
        if (events.size() < parameters.getFrom()) {
            log.info("Value 'from'({}) above result list size({}). Returning empty list.", parameters.getFrom(), events.size());
            return new ArrayList<>();
        }
        events = events.subList(parameters.getFrom(), events.size());
        if (events.size() > parameters.getSize()) {
            events = events.subList(0, parameters.getSize());
        }
        if (sortType.equals(PublicSearchEventSort.EVENT_DATE)) {
            return events.stream()
                    .sorted(Comparator.comparing(Event::getEventDate))
                    .map(utils::convertEventToShortDto)
                    .collect(Collectors.toList());
        }
        return events.stream()
                .map(utils::convertEventToShortDto)
                .sorted(Comparator.comparing(EventShortDto::getViews))
                .collect(Collectors.toList());
    }
}
