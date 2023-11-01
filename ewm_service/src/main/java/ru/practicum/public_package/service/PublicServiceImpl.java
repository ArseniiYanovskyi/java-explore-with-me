package ru.practicum.public_package.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dao.CategoryRepository;
import ru.practicum.dao.EventRepository;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.EventShortDto;
import ru.practicum.model.event.dto.PublicSearchEventParameters;
import ru.practicum.model.event.dto.PublicSearchEventSort;
import ru.practicum.model.exception.NotFoundException;
import ru.practicum.public_package.service.utils.PublicServiceUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicServiceImpl implements PublicService{
    private final PublicServiceUtils utils;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    @Override
    public List<CategoryDto> getCategoriesList(int from, int size) {
        log.info("Sending to repository request to get categories list.");
        List<Category> categoryList = categoryRepository.findAll();
        if (categoryList.size() < from) {
            log.info("Value 'from'({}) above result list size({}). Returning empty list.", from, categoryList.size());
            return new ArrayList<>();
        }
        categoryList = categoryList.subList(from, categoryList.size());
        if (categoryList.size() <= size) {
            return categoryList.stream()
                    .map(utils::convertCategoryToDto)
                    .collect(Collectors.toList());
        } else {
            return categoryList.subList(0, size-1).stream()
                    .map(utils::convertCategoryToDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public CategoryDto getCategoryById(long categoryId) {
        return utils.convertCategoryToDto(categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id " + categoryId + " does not present in repository.")));
    }

    @Override
    public List<EventShortDto> getEventsByParameters(PublicSearchEventParameters parameters) {
        PublicSearchEventSort sortType = PublicSearchEventSort.parseSearchSort(parameters.getSortType())
                .orElseThrow(() -> new NotFoundException("Such sort type can not be used is search."));
        List<Event> repositoryResult = new ArrayList<>();

        if (parameters.getRangeStart() != null && parameters.getRangeEnd() != null) {
            Timestamp start = Timestamp.valueOf(LocalDateTime.parse(parameters.getRangeStart(), Mapper.formatter));
            Timestamp end = Timestamp.valueOf(LocalDateTime.parse(parameters.getRangeEnd(), Mapper.formatter));
            if (sortType.equals(PublicSearchEventSort.EVENT_DATE)) {
                repositoryResult = eventRepository.findAllByStateIsAndCategoryIdInAndEventDateIsBetweenOrderByEventDateAsc(State.PUBLISHED, parameters.getCategories(), start, end);
            } else if (sortType.equals(PublicSearchEventSort.VIEWS)) {
                repositoryResult = eventRepository.findAllByStateIsAndCategoryIdInAndEventDateIsBetweenOrderByViewsDesc(State.PUBLISHED, parameters.getCategories(), start, end);
            }
        } else {
            if (sortType.equals(PublicSearchEventSort.EVENT_DATE)) {
                repositoryResult = eventRepository.findAllByStateIsAndEventDateIsAfterAndCategoryIdInOrderByEventDateAsc(State.PUBLISHED, Timestamp.valueOf(LocalDateTime.now()), parameters.getCategories());
            } else if (sortType.equals(PublicSearchEventSort.VIEWS)) {
                repositoryResult = eventRepository.findAllByStateIsAndEventDateIsAfterAndCategoryIdInOrderByViewsDesc(State.PUBLISHED, Timestamp.valueOf(LocalDateTime.now()), parameters.getCategories());
            }
        }
        repositoryResult = repositoryResult.stream()
                .filter(event -> event.isPaid() == parameters.getPaid())
                .filter(event -> event.getParticipantLimit() == 0 || event.getConfirmedRequests() < event.getParticipantLimit())
                .filter(event -> event.getAnnotation().toLowerCase().contains(parameters.getText().toLowerCase()) ||
                        event.getDescription().toLowerCase().contains(parameters.getText().toLowerCase()))
                .collect(Collectors.toList());
        List<EventShortDto> result = formResultForEventSearch(repositoryResult, parameters);
        for (Event event : repositoryResult) {
            event.setViews(event.getViews()+1);
        }
        log.info("Sending to repository request to update events (add extra view).");
        eventRepository.saveAll(repositoryResult);
        return result;
    }

    @Override
    public EventShortDto getEventById(long eventId) {
        log.info("Sending to repository request to get published event with id {}.", eventId);
        Event event = eventRepository.findByIdIsAndStateIs(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Published event with id " + eventId + " does not present in repository."));
        EventShortDto result = utils.convertEventToShortDto(event);
        event.setViews(event.getViews()+1);
        log.info("Sending to repository request to update event {} to add extra view.", eventId);
        eventRepository.save(event);
        return result;
    }

    private List<EventShortDto> formResultForEventSearch(List<Event> events, PublicSearchEventParameters parameters) {
        log.info("Repository answered {}, forming answer to controller.", events);
        if (events.size() < parameters.getFrom()) {
            log.info("Value 'from'({}) above result list size({}). Returning empty list.", parameters.getFrom(), events.size());
            return new ArrayList<>();
        }
        events = events.subList(parameters.getFrom(), events.size());
        if (events.size() <= parameters.getSize()) {
            return events.stream()
                    .map(utils::convertEventToShortDto)
                    .collect(Collectors.toList());
        } else {
            return events.subList(0, parameters.getSize()-1).stream()
                    .map(utils::convertEventToShortDto)
                    .collect(Collectors.toList());
        }
    }
}
