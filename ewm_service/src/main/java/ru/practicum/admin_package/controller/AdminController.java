package ru.practicum.admin_package.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.user.dto.UserDto;
import ru.practicum.admin_package.service.AdminService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/categories")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody CategoryDto categoryDto) {
        log.info("Received request to add new category of event: {}.", categoryDto.getName());
        return adminService.addCategory(categoryDto);
    }

    @PatchMapping("/categories/{catId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable(value = "catId") long catId, @RequestBody CategoryDto categoryDto) {
        log.info("Received request to update category with id {} to a new name: {}.", catId, categoryDto.getName());
        return adminService.updateCategory(catId, categoryDto);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable(value = "catId") long catId) {
        log.info("Received request to delete category with id {}.", catId);
        adminService.deleteCategory(catId);
    }

    @PostMapping("/users")
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserDto addUser(@RequestBody UserDto userDto) {
        log.info("Received request to add new user name: {}. email: {}.", userDto.getName(), userDto.getEmail());
        return adminService.addUser(userDto);
    }

    @GetMapping("/users")
    @ResponseStatus(code = HttpStatus.OK)
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") int from,
                                  @RequestParam (defaultValue = "10") int size) {
        if (ids == null) {
            ids = new ArrayList<>();
        }
        log.info("Received request to get users list from {}, size {}.", from, size);
        return adminService.getUsersList(ids, from, size);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable(value = "userId") long userId) {
        log.info("Received request to delete user with id {}.", userId);
        adminService.deleteUser(userId);
    }
}
