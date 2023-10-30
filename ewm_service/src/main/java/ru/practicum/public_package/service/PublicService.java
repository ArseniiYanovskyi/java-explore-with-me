package ru.practicum.public_package.service;

import ru.practicum.model.category.dto.CategoryDto;

import java.util.List;

public interface PublicService {
    public List<CategoryDto> getCategoriesList(int from, int size);
    public CategoryDto getCategoryById(long categoryId);
}
