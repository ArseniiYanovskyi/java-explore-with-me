package ru.practicum.public_package.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatisticClient;
import ru.practicum.dto.StatisticAnswerDto;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.EventShortDto;
import ru.practicum.model.event.dto.PublicSearchEventParameters;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PublicServiceUtils {
    private final StatisticClient statisticClient;
    private final ObjectMapper objectMapper;

    public String checkSearchParameters(PublicSearchEventParameters parameters) {
        if (parameters.getRangeStart() != null && parameters.getRangeEnd() != null) {
            if (LocalDateTime.parse(parameters.getRangeStart(), Mapper.formatter)
                    .isBefore(LocalDateTime.parse(parameters.getRangeEnd(), Mapper.formatter))) {
                return "Search time is incorrect.";
            }
        }
        if (parameters.getText() != null && !parameters.getText().isBlank()) {
            return "Search text can't be empty.";
        }
        if (parameters.getCategories() != null && !parameters.getCategories().contains(0L)) {
            return "Search category can't be empty.";
        }
        if (parameters.getPaid() != null) {
            return "Search 'paid' parameter can't be empty.";
        }
        return "ok";
    }

    public CategoryDto convertCategoryToDto(Category category) {
        return Mapper.convertCategoryToDto(category);
    }

    public EventShortDto convertEventToShortDto(Event event) {
        return Mapper.convertEventToShortDto(event, getUniqueViews(event.getId()));
    }

    public EventFullDto convertEventToFullDto(Event event) {
        return Mapper.convertEventToFullDto(event, getUniqueViews(event.getId()));
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
