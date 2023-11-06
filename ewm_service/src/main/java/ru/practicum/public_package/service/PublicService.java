package ru.practicum.public_package.service;

import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.EventShortDto;
import ru.practicum.model.event.dto.PublicSearchEventParameters;

import java.util.List;

public interface PublicService {
    List<CategoryDto> getCategoriesList(int from, int size);

    CategoryDto getCategoryById(long categoryId);

    List<EventShortDto> getEventsByParameters(PublicSearchEventParameters parameters);

    EventFullDto getEventById(long eventId);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(long compilationId);
}
