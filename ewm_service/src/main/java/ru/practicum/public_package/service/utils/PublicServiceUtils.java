package ru.practicum.public_package.service.utils;

import org.springframework.stereotype.Component;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.dto.EventShortDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PublicServiceUtils {
    public Category convertCategoryFromDto(CategoryDto categoryDto) {
        return Mapper.convertCategoryFromDto(categoryDto);
    }

    public CategoryDto convertCategoryToDto(Category category) {
        return Mapper.convertCategoryToDto(category);
    }
    public EventShortDto convertEventToShortDto(Event event) {
        return Mapper.convertEventToShortDto(event);
    }
    public CompilationDto convertCompilationToDto(Compilation compilation, List<Event> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(events.stream()
                        .map(Mapper::convertEventToShortDto)
                        .collect(Collectors.toList()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}
