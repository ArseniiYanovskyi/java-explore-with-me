package ru.practicum.users.cotroller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.users.model.dto.UserDto;
import ru.practicum.users.service.UserService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping("/admin/users")
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserDto addUser(@RequestBody @Valid UserDto userDto) {
        log.info("Received request to add new user name: {}. email: {}.", userDto.getName(), userDto.getEmail());
        return userService.addUser(userDto);
    }

    @GetMapping("/admin/users")
    @ResponseStatus(code = HttpStatus.OK)
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") int from,
                                  @RequestParam(defaultValue = "10") int size) {
        if (ids == null) {
            ids = new ArrayList<>();
        }
        log.info("Received request to get users list from {}, size {}.", from, size);
        return userService.getUsersList(ids, from, size);
    }

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable(value = "userId") long userId) {
        log.info("Received request to delete user with id {}.", userId);
        userService.deleteUser(userId);
    }
}
