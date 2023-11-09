package ru.practicum.users.service;

import ru.practicum.users.model.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(UserDto userDto);

    List<UserDto> getUsersList(List<Long> ids, int from, int size);

    void deleteUser(long id);
}
