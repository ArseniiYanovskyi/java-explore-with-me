package ru.practicum.mapper;

import ru.practicum.category.model.Category;
import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.State;
import ru.practicum.event.model.dto.EventFullDto;
import ru.practicum.event.model.dto.EventShortDto;
import ru.practicum.event.model.dto.NewEventDto;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.Status;
import ru.practicum.request.model.dto.ParticipationRequestDto;
import ru.practicum.users.model.User;
import ru.practicum.users.model.dto.UserDto;
import ru.practicum.users.model.dto.UserShortDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Mapper {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Category convertCategoryFromDto(CategoryDto categoryDto) {
        return Category.builder()
                .id(categoryDto.getId() == 0 ? 0 : categoryDto.getId())
                .name(categoryDto.getName())
                .build();
    }

    public static CategoryDto convertCategoryToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static User convertUserFromDto(UserDto userDto) {
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static UserDto convertUserToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static UserShortDto convertUserToShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public static Event convertNewEventFromDto(NewEventDto newEventDto, User initiator, Category category) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .initiator(initiator)
                .description(newEventDto.getDescription())
                .eventDate(LocalDateTime.parse(newEventDto.getEventDate(), formatter))
                .createdOn(LocalDateTime.now())
                .latitude(newEventDto.getLocation().getLat())
                .longitude(newEventDto.getLocation().getLon())
                .paid(newEventDto.getPaid())
                .requestModeration(newEventDto.getRequestModeration())
                .participantLimit(newEventDto.getParticipantLimit())
                .title(newEventDto.getTitle())
                .confirmedRequests(0)
                .state(State.PENDING)
                .build();
    }

    public static EventFullDto convertEventToFullDto(Event event, long views) {
        String publishedTime = "";
        if (event.getPublishedTime() != null) {
            publishedTime = event.getPublishedTime().format(formatter);
        }
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(convertCategoryToDto(event.getCategory()))
                .initiator(convertUserToShortDto(event.getInitiator()))
                .createdOn(event.getCreatedOn().format(formatter))
                .publishedOn(publishedTime)
                .eventDate(event.getEventDate().format(formatter))
                .location(new Location(event.getLatitude(), event.getLongitude()))
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(event.getConfirmedRequests())
                .requestModeration(event.isRequestModeration())
                .state(event.getState())
                .views(views)
                .build();
    }

    public static EventShortDto convertEventToShortDto(Event event, long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .category(convertCategoryToDto(event.getCategory()))
                .initiator(convertUserToShortDto(event.getInitiator()))
                .eventDate(event.getEventDate().format(formatter))
                .paid(event.isPaid())
                .confirmedRequests(event.getConfirmedRequests())
                .views(views)
                .build();
    }

    public static Request createRequest(User user, Event event) {
        return Request.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(Status.PENDING)
                .build();
    }

    public static ParticipationRequestDto createParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .created(request.getCreated().format(formatter))
                .status(request.getStatus())
                .build();
    }
}
