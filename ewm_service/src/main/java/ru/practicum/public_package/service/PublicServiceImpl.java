package ru.practicum.public_package.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dao.CategoryRepository;
import ru.practicum.dao.CompilationRepository;
import ru.practicum.dao.CompilationsEventsDB;
import ru.practicum.dao.EventRepository;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.EventShortDto;
import ru.practicum.model.event.dto.PublicSearchEventParameters;
import ru.practicum.model.event.dto.PublicSearchEventSort;
import ru.practicum.model.exception.BadRequestException;
import ru.practicum.model.exception.NotFoundException;
import ru.practicum.public_package.service.utils.PublicServiceUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicServiceImpl implements PublicService {
    private final PublicServiceUtils utils;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final CompilationsEventsDB compilationsEventsRepository;

    @Override
    public List<CategoryDto> getCategoriesList(int from, int size) {
        log.info("Sending to repository request to get categories list.");
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(utils::convertCategoryToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(long categoryId) {
        return utils.convertCategoryToDto(categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id " + categoryId + " does not present in repository.")));
    }

    @Override
    public List<EventShortDto> getEventsByParameters(PublicSearchEventParameters parameters) {
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

        return formResultForEventSearch(events, parameters);
    }

    @Override
    public EventFullDto getEventById(long eventId) {
        log.info("Sending to repository request to get published event with id {}.", eventId);
        Event event = eventRepository.findByIdIsAndStateIs(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Published event with id " + eventId + " does not present in repository."));
        return utils.convertEventToFullDto(event);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (pinned == null) {
            return compilationRepository.findAll(pageable).stream()
                    .map(compilation -> utils.convertCompilationToDto(compilation, eventRepository.findAllById(compilationsEventsRepository.getCompilationEvents(compilation.getId()))))
                    .collect(Collectors.toList());
        }
        log.info("Sending to repository request to get Pinned({}) compilations.", pinned);
        return compilationRepository.findAllByPinnedIs(pinned, pageable).stream()
                .map(compilation -> utils.convertCompilationToDto(compilation, eventRepository.findAllById(compilationsEventsRepository.getCompilationEvents(compilation.getId()))))
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(long compilationId) {
        log.info("Sending to repository request to get compilation with id {}.", compilationId);
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation with id " + compilationId + " does not present in repository."));
        List<Event> compilationEvents = compilationsEventsRepository.getCompilationEvents(compilationId).stream()
                .map(eventRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return utils.convertCompilationToDto(compilation, compilationEvents);
    }

    private List<EventShortDto> formResultForEventSearch(List<Event> events, PublicSearchEventParameters parameters) {
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
