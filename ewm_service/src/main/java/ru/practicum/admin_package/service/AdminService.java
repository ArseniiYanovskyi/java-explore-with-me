package ru.practicum.admin_package.service;

import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.compilation.dto.NewCompilationDto;
import ru.practicum.model.compilation.dto.UpdateCompilationRequest;
import ru.practicum.model.event.dto.AdminEventSearchParameters;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.UpdateEventAdminRequest;
import ru.practicum.model.user.dto.UserDto;

import java.util.List;

public interface AdminService {
    CategoryDto addCategory(CategoryDto categoryDto);

    CategoryDto updateCategory(long categoryId, CategoryDto categoryDto);

    EventFullDto updateEvent(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventFullDto> searchEvent(AdminEventSearchParameters adminEventSearchParameters);

    void deleteCategory(long categoryId);

    UserDto addUser(UserDto userDto);

    List<UserDto> getUsersList(List<Long> ids, int from, int size);

    void deleteUser(long id);

    CompilationDto addNewCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(long compilationId, UpdateCompilationRequest updateCompilationRequest);

    void deleteCompilation(long compilationId);
}
