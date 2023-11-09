package ru.practicum.category.service;

import ru.practicum.category.model.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(CategoryDto categoryDto);

    CategoryDto updateCategory(long categoryId, CategoryDto categoryDto);

    List<CategoryDto> getCategoriesList(int from, int size);

    CategoryDto getCategoryById(long categoryId);

    void deleteCategory(long categoryId);
}
