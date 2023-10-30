package ru.practicum.admin_package.service;

import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.user.dto.UserDto;

import java.util.List;

public interface AdminService {
    CategoryDto addCategory(CategoryDto categoryDto);
    CategoryDto updateCategory(long categoryId, CategoryDto categoryDto);
    void deleteCategory(long categoryId);
    UserDto addUser(UserDto userDto);
    List<UserDto> getUsersList(List<Long> ids, int from, int size);
    void deleteUser(long id);
}
