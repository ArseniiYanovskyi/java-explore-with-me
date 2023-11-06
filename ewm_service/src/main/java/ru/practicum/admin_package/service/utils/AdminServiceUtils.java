package ru.practicum.admin_package.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatisticClient;
import ru.practicum.dao.CategoryRepository;
import ru.practicum.dto.StatisticAnswerDto;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.compilation.dto.NewCompilationDto;
import ru.practicum.model.compilation.dto.UpdateCompilationRequest;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.StateAction;
import ru.practicum.model.event.dto.UpdateEventAdminRequest;
import ru.practicum.model.exception.ConflictRequestException;
import ru.practicum.model.exception.NotFoundException;
import ru.practicum.model.user.User;
import ru.practicum.model.user.dto.UserDto;
import ru.practicum.validation.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class AdminServiceUtils {
    private final CategoryRepository categoryRepository;
    private final StatisticClient statisticClient;
    private final ObjectMapper objectMapper;

    public Category convertCategoryFromDto(CategoryDto categoryDto) {
        return Mapper.convertCategoryFromDto(categoryDto);
    }

    public Event updateEventObject(Event event, UpdateEventAdminRequest updateEventAdminRequest) {
        if (updateEventAdminRequest.getEventDate() != null) {
            Validator.checkEventDateAdmin(updateEventAdminRequest.getEventDate());

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
            Validator.checkAnnotation(updateEventAdminRequest.getAnnotation());

            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getDescription() != null && !updateEventAdminRequest.getDescription().isBlank()) {
            Validator.checkDescription(updateEventAdminRequest.getDescription());

            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getTitle() != null && !updateEventAdminRequest.getTitle().isBlank()) {
            Validator.checkTitle(updateEventAdminRequest.getTitle());

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

    public EventFullDto convertEventToFullDto(Event event) {
        return Mapper.convertEventToFullDto(event, getUniqueViews(event.getId()));
    }

    public CategoryDto convertCategoryToDto(Category category) {
        return Mapper.convertCategoryToDto(category);
    }

    public void checkCategoryDto(CategoryDto categoryDto) {
        Validator.checkCategoryName(categoryDto.getName());
    }

    public void checkUserDto(UserDto userDto) {
        Validator.checkUserName(userDto.getName());
        Validator.checkUserEmail(userDto.getEmail());
    }

    public User convertUserFromDto(UserDto userDto) {
        return Mapper.convertUserFromDto(userDto);
    }

    public UserDto convertUserToDto(User user) {
        return Mapper.convertUserToDto(user);
    }

    public void checkCompilationNewDto(NewCompilationDto newCompilationDto) {
        Validator.checkCompilationTitle(newCompilationDto.getTitle());
    }

    public void checkCompilationUpdatingDto(UpdateCompilationRequest updateCompilationRequest) {
        if (updateCompilationRequest.getTitle() != null) {
            Validator.checkCompilationTitle(updateCompilationRequest.getTitle());
        }
    }

    public Compilation convertCompilationFromDto(NewCompilationDto newCompilationDto) {
        if (newCompilationDto.getPinned() == null) {
            newCompilationDto.setPinned(false);
        }
        if (newCompilationDto.getEvents() == null) {
            newCompilationDto.setEvents(new ArrayList<>());
        }
        return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned())
                .build();
    }

    public CompilationDto convertCompilationToDto(Compilation compilation, List<Event> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(events.stream()
                        .map(event -> Mapper.convertEventToShortDto(event, getUniqueViews(event.getId())))
                        .collect(Collectors.toList()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public Compilation updateCompilationObject(Compilation compilation, UpdateCompilationRequest updateCompilationRequest) {
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        return compilation;
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
