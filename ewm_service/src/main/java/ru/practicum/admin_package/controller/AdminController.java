package ru.practicum.admin_package.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.admin_package.service.AdminService;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.compilation.dto.NewCompilationDto;
import ru.practicum.model.compilation.dto.UpdateCompilationRequest;
import ru.practicum.model.event.dto.AdminEventSearchParameters;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.UpdateEventAdminRequest;
import ru.practicum.model.user.dto.UserDto;

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

    @PostMapping("/users")
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserDto addUser(@RequestBody UserDto userDto) {
        log.info("Received request to add new user name: {}. email: {}.", userDto.getName(), userDto.getEmail());
        return adminService.addUser(userDto);
    }

    @PostMapping("/compilations")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody NewCompilationDto newCompilationDto) {
        log.info("Received request to add new compilation title: {}, pinned: {}, events: {}.", newCompilationDto.getTitle(), newCompilationDto.getPinned(), newCompilationDto.getEvents());
        return adminService.addNewCompilation(newCompilationDto);
    }

    @PatchMapping("/categories/{catId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable long catId, @RequestBody CategoryDto categoryDto) {
        log.info("Received request to update category with id {} to a new name: {}.", catId, categoryDto.getName());
        return adminService.updateCategory(catId, categoryDto);
    }

    @PatchMapping("/events/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable long eventId, @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Received request to update event {}.", eventId);
        return adminService.updateEvent(eventId, updateEventAdminRequest);
    }

    @PatchMapping("/compilations/{compId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable(value = "compId") long compId, @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        log.info("Received request to update compilation with id {}.", compId);
        return adminService.updateCompilation(compId, updateCompilationRequest);
    }

    @GetMapping("/users")
    @ResponseStatus(code = HttpStatus.OK)
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") int from,
                                  @RequestParam(defaultValue = "10") int size) {
        if (ids == null) {
            ids = new ArrayList<>();
        }
        log.info("Received request to get users list from {}, size {}.", from, size);
        return adminService.getUsersList(ids, from, size);
    }

    @GetMapping("/events")
    @ResponseStatus(code = HttpStatus.OK)
    public List<EventFullDto> searchEvents(@RequestParam(required = false) List<Long> users,
                                           @RequestParam(required = false) List<String> states,
                                           @RequestParam(required = false) List<Long> categories,
                                           @RequestParam(required = false) String rangeStart,
                                           @RequestParam(required = false) String rangeEnd,
                                           @RequestParam(defaultValue = "0") int from,
                                           @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to search events from users {} in {} states, in {} categories from time {} to time {}.",
                users, states, categories, rangeStart, rangeEnd);
        return adminService.searchEvent(AdminEventSearchParameters.builder()
                .usersIds(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build());
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable(value = "userId") long userId) {
        log.info("Received request to delete user with id {}.", userId);
        adminService.deleteUser(userId);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable(value = "catId") long catId) {
        log.info("Received request to delete category with id {}.", catId);
        adminService.deleteCategory(catId);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable(value = "compId") long compId) {
        log.info("Received request to delete compilation with id {}.", compId);
        adminService.deleteCompilation(compId);
    }
}
