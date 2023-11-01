package ru.practicum.admin_package.service.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dao.CategoryRepository;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.StateAction;
import ru.practicum.model.event.dto.UpdateEventAdminRequest;
import ru.practicum.model.exception.IncorrectRequestException;
import ru.practicum.model.exception.NotFoundException;
import ru.practicum.model.user.User;
import ru.practicum.model.user.dto.UserDto;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class AdminServiceUtils {
    private final CategoryRepository categoryRepository;

    public Category convertCategoryFromDto(CategoryDto categoryDto) {
        return Mapper.convertCategoryFromDto(categoryDto);
    }

    public Event updateEventObject(Event event, UpdateEventAdminRequest updateEventAdminRequest) {
        if (LocalDateTime.parse(updateEventAdminRequest.getEventDate(), Mapper.formatter).isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IncorrectRequestException("Time of updated by admin event can't be earlier than 1 hours later.");
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (!event.getState().equals(State.PENDING)) {
                throw new IncorrectRequestException("Incorrect administrator state action.");
            }
            if (updateEventAdminRequest.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
                event.setState(State.PUBLISHED);
            }
            if (updateEventAdminRequest.getStateAction().equals(StateAction.REJECT_EVENT)) {
                event.setState(State.CANCELED);
            }
        }
        if (updateEventAdminRequest.getCategory() != null && updateEventAdminRequest.getCategory() != 0) {
            Category newCategory = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id " + updateEventAdminRequest.getCategory() + " does not present in repository."));
            event.setCategory(newCategory);
        }
        if (updateEventAdminRequest.getAnnotation() != null && !updateEventAdminRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getDescription() != null && !updateEventAdminRequest.getDescription().isBlank()) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getTitle() != null && !updateEventAdminRequest.getTitle().isBlank()) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
        if (updateEventAdminRequest.getEventDate() != null && !updateEventAdminRequest.getEventDate().isBlank()) {
            event.setEventDate(LocalDateTime.parse(updateEventAdminRequest.getEventDate(), Mapper.formatter));
        }
        if (updateEventAdminRequest.getLocation() != null && updateEventAdminRequest.getLocation().getLat() != 0 && updateEventAdminRequest.getLocation().getLen() != 0) {
            event.setLatitude(updateEventAdminRequest.getLocation().getLat());
            event.setLongitude(updateEventAdminRequest.getLocation().getLen());
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

    public EventFullDto convertEventToFullDto(Event event) {
        return Mapper.convertEventToFullDto(event);
    }

    public CategoryDto convertCategoryToDto(Category category) {
        return Mapper.convertCategoryToDto(category);
    }

    public User convertUserFromDto(UserDto userDto) {
        return Mapper.convertUserFromDto(userDto);
    }

    public UserDto convertUserToDto(User user) {
        return Mapper.convertUserToDto(user);
    }
}
