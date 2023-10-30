package ru.practicum.admin_package.service.utils;

import org.springframework.stereotype.Component;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.user.User;
import ru.practicum.model.user.dto.UserDto;

@Component
public class AdminServiceUtils {
    public Category convertCategoryFromDto(CategoryDto categoryDto) {
        return Mapper.convertCategoryFromDto(categoryDto);
    }

    public CategoryDto convertCategoryToDto(Category category) {
        return Mapper.convertCategoryToDto(category);
    }

    public User convertUserFromDto(UserDto userDto) {
        return Mapper.convertUserFromDto(userDto);
    }

    public UserDto convertUserToDto(User user) {
        return Mapper.convertUserToDto(user);
    }
}
