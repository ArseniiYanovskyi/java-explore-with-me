package ru.practicum.public_package.service.utils;

import org.springframework.stereotype.Component;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;

@Component
public class PublicServiceUtils {
    public Category convertCategoryFromDto(CategoryDto categoryDto) {
        return Mapper.convertCategoryFromDto(categoryDto);
    }

    public CategoryDto convertCategoryToDto(Category category) {
        return Mapper.convertCategoryToDto(category);
    }
}
