package ru.practicum.public_package.service;

import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.event.dto.EventShortDto;
import ru.practicum.model.event.dto.PublicSearchEventParameters;

import java.util.List;

public interface PublicService {
    public List<CategoryDto> getCategoriesList(int from, int size);
    public CategoryDto getCategoryById(long categoryId);
    public List<EventShortDto> getEventsByParameters(PublicSearchEventParameters parameters);
    public EventShortDto getEventById(long eventId);
}
