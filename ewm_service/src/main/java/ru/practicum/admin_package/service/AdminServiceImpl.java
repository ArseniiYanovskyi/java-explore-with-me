package ru.practicum.admin_package.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.admin_package.service.utils.AdminServiceUtils;
import ru.practicum.dao.*;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.compilation.dto.NewCompilationDto;
import ru.practicum.model.compilation.dto.UpdateCompilationRequest;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;
import ru.practicum.model.event.dto.AdminEventSearchParameters;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.UpdateEventAdminRequest;
import ru.practicum.model.exception.ConflictRequestException;
import ru.practicum.model.exception.NotFoundException;
import ru.practicum.model.user.User;
import ru.practicum.model.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final AdminServiceUtils utils;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final CompilationsEventsDB compilationsEventsRepository;

    @Override
    public CategoryDto addCategory(CategoryDto categoryDto) {
        utils.checkCategoryDto(categoryDto);
        Optional<Category> optionalCategory = categoryRepository.findByName(categoryDto.getName());
        if (optionalCategory.isPresent()) {
            log.info("Category with this name already present in repository.");
            throw new ConflictRequestException("Category with this name already present in repository.");
        }

        log.info("Sending to repository request to add new category: {}.", categoryDto.getName());
        Category category = categoryRepository.save(utils.convertCategoryFromDto(categoryDto));
        return utils.convertCategoryToDto(category);
    }

    @Override
    public CategoryDto updateCategory(long categoryId, CategoryDto categoryDto) {
        utils.checkCategoryDto(categoryDto);
        Optional<Category> optionalCategory = categoryRepository.findByName(categoryDto.getName());
        if (optionalCategory.isPresent() && optionalCategory.get().getId() != categoryId) {
            log.info("Category with this name already present in repository.");
            throw new ConflictRequestException("Category with this name already present in repository.");
        }

        log.info("Sending to repository request to update category with id {} to: {}.", categoryId, categoryDto.getName());
        Category category = categoryRepository.save(utils.convertCategoryFromDto(categoryDto));
        return utils.convertCategoryToDto(category);
    }

    @Override
    public EventFullDto updateEvent(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event updatedEvent = utils.updateEventObject(eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " does not present in repository.")),
                updateEventAdminRequest);
        log.info("Sending to repository request to update event {}.", eventId);
        return utils.convertEventToFullDto(eventRepository.save(updatedEvent));
    }

    @Override
    public List<EventFullDto> searchEvent(AdminEventSearchParameters searchParameters) {
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
        return formResultForEventSearch(events, searchParameters.getFrom(), searchParameters.getSize());
    }

    @Override
    public void deleteCategory(long categoryId) {
        if (!eventRepository.findAllByCategoryId(categoryId).isEmpty()) {
            throw new ConflictRequestException("In repository presents events connected to this category.");
        }
        log.info("Sending to repository request to delete category with id {}.", categoryId);
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        utils.checkUserDto(userDto);
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ConflictRequestException("User with this email already present in repository.");
        }
        log.info("Sending to repository request to add new user. name: {}. email: {}.", userDto.getName(), userDto.getEmail());
        User user = userRepository.save(utils.convertUserFromDto(userDto));
        return utils.convertUserToDto(user);
    }

    @Override
    public List<UserDto> getUsersList(List<Long> ids, int from, int size) {
        log.info("Sending to repository request to get user list. ids: {}", ids);
        if (ids.isEmpty()) {
            return formResultForUserListRequest(userRepository.findAll(), from, size);

        }
        return formResultForUserListRequest(userRepository.findByIdIn(ids), from, size);
    }

    @Override
    public void deleteUser(long id) {
        log.info("Sending to repository request to delete user with id {}.", id);
        userRepository.deleteById(id);
    }

    @Override
    public CompilationDto addNewCompilation(NewCompilationDto newCompilationDto) {
        utils.checkCompilationNewDto(newCompilationDto);
        Compilation compilation = utils.convertCompilationFromDto(newCompilationDto);
        log.info("Sending to repository request to save new compilation.");
        compilation = compilationRepository.save(compilation);
        log.info("Sending to repository request to save compilation events id's.");
        compilationsEventsRepository.saveCompilationEvents(compilation.getId(), newCompilationDto.getEvents());
        log.info("Sending to repository request to get new compilation events: {}.", newCompilationDto.getEvents());
        List<Event> compilationEvents = eventRepository.findAllById(newCompilationDto.getEvents());
        return utils.convertCompilationToDto(compilation, compilationEvents);
    }

    @Override
    public CompilationDto updateCompilation(long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        utils.checkCompilationUpdatingDto(updateCompilationRequest);
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compilationId + " does not present in repository."));
        if (updateCompilationRequest.getEvents() != null) {
            log.info("Sending to repository request to delete old compilation events id's.");
            compilationsEventsRepository.deleteCompilationEvents(compilationId);
            log.info("Sending to repository request to save compilation events id's: {}.", updateCompilationRequest.getEvents());
            compilationsEventsRepository.saveCompilationEvents(compilationId, updateCompilationRequest.getEvents());
        }
        compilation = utils.updateCompilationObject(compilation, updateCompilationRequest);
        log.info("Sending to repository request to update compilation.");
        compilationRepository.save(compilation);
        List<Event> compilationEvents = eventRepository.findAllById(compilationsEventsRepository.getCompilationEvents(compilationId));
        return utils.convertCompilationToDto(compilation, compilationEvents);
    }

    @Override
    public void deleteCompilation(long compilationId) {
        compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compilationId + " does not present in repository."));
        log.info("Sending to repository request to delete compilation with id {}.", compilationId);
        compilationRepository.deleteById(compilationId);
    }

    private List<UserDto> formResultForUserListRequest(List<User> userList, int from, int size) {
        log.info("Repository answered {}, forming answer to controller.", userList);
        if (userList.size() < from) {
            log.info("Value 'from'({}) above result list size({}). Returning empty list.", from, userList.size());
            return new ArrayList<>();
        }
        userList = userList.subList(from, userList.size());
        if (userList.size() <= size) {
            return userList.stream()
                    .map(utils::convertUserToDto)
                    .collect(Collectors.toList());
        } else {
            return userList.subList(0, size).stream()
                    .map(utils::convertUserToDto)
                    .collect(Collectors.toList());
        }
    }

    private List<EventFullDto> formResultForEventSearch(List<Event> events, int from, int size) {
        log.info("Repository answered {}, forming answer to controller.", events);
        if (events.size() < from) {
            log.info("Value 'from'({}) above result list size({}). Returning empty list.", from, events.size());
            return new ArrayList<>();
        }
        events = events.subList(from, events.size());
        if (events.size() <= size) {
            return events.stream()
                    .map(utils::convertEventToFullDto)
                    .collect(Collectors.toList());
        } else {
            return events.subList(0, size).stream()
                    .map(utils::convertEventToFullDto)
                    .collect(Collectors.toList());
        }
    }
}