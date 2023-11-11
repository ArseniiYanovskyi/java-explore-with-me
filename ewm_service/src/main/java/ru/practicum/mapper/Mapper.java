package ru.practicum.mapper;

import ru.practicum.category.model.Category;
import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentReply;
import ru.practicum.comment.model.dto.CommentDto;
import ru.practicum.comment.model.dto.CommentReplyDto;
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
import java.util.List;
import java.util.stream.Collectors;

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

    public static EventFullDto convertEventToFullDto(Event event, List<Comment> comments, List<CommentReply> replies, long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(convertCategoryToDto(event.getCategory()))
                .initiator(convertUserToShortDto(event.getInitiator()))
                .createdOn(event.getCreatedOn().format(formatter))
                .publishedOn(event.getPublishedTime() == null ? null : event.getPublishedTime().format(formatter))
                .eventDate(event.getEventDate().format(formatter))
                .location(new Location(event.getLatitude(), event.getLongitude()))
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(event.getConfirmedRequests())
                .requestModeration(event.isRequestModeration())
                .state(event.getState())
                .comments(comments.stream()
                        .map(comment -> Mapper.convertCommentToDto(comment, replies.stream()
                                        .filter(reply -> reply.getComment().getId() == comment.getId())
                                        .collect(Collectors.toList())))
                        .collect(Collectors.toList()))
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

    public static Comment createComment(User commentator, Event event, CommentDto commentDto) {
        return Comment.builder()
                .commentator(commentator)
                .event(event)
                .text(commentDto.getText())
                .timestamp(LocalDateTime.now())
                .edited(false)
                .editedTime(null)
                .build();
    }

    public static CommentDto convertCommentToDto(Comment comment, List<CommentReply> replies) {
        return CommentDto.builder()
                .id(comment.getId())
                .commentatorId(comment.getCommentator().getId())
                .eventId(comment.getEvent().getId())
                .text(comment.getText())
                .timestamp(comment.getTimestamp().format(formatter))
                .replies(replies.stream()
                        .map(Mapper::convertCommentReplyToDto)
                        .collect(Collectors.toList()))
                .edited(comment.isEdited())
                .editedTime(comment.getEditedTime() == null ? null : comment.getEditedTime().format(formatter))
                .build();
    }

    public static CommentReply createCommentReply(User commentator, Comment comment, CommentReplyDto commentReplyDto) {
        return CommentReply.builder()
                .commentator(commentator)
                .comment(comment)
                .text(commentReplyDto.getText())
                .timestamp(LocalDateTime.now())
                .edited(false)
                .editedTime(null)
                .build();
    }

    public static CommentReplyDto convertCommentReplyToDto(CommentReply commentReply) {
        return CommentReplyDto.builder()
                .id(commentReply.getId())
                .commentatorId(commentReply.getCommentator().getId())
                .commentId(commentReply.getComment().getId())
                .text(commentReply.getText())
                .timestamp(commentReply.getTimestamp().format(formatter))
                .edited(commentReply.isEdited())
                .editedTime(commentReply.getEditedTime() == null ? null : commentReply.getEditedTime().format(formatter))
                .build();
    }
}
