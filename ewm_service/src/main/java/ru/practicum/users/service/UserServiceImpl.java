package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.exception.model.ConflictRequestException;
import ru.practicum.utils.Mapper;
import ru.practicum.users.dao.UserRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.model.dto.UserDto;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ConflictRequestException("User with this email already present in repository.");
        }
        log.info("Sending to repository request to add new user. name: {}. email: {}.", userDto.getName(), userDto.getEmail());
        User user = userRepository.save(Mapper.convertUserFromDto(userDto));
        return Mapper.convertUserToDto(user);
    }

    @Override
    @Transactional
    public List<UserDto> getUsersList(List<Long> ids, int from, int size) {
        log.info("Sending to repository request to get user list. ids: {}", ids);
        if (ids.isEmpty()) {
            return formResultForUserListRequest(userRepository.findAll(), from, size);

        }
        return formResultForUserListRequest(userRepository.findByIdIn(ids), from, size);
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        log.info("Sending to repository request to delete user with id {}.", id);
        userRepository.deleteById(id);
    }

    private List<UserDto> formResultForUserListRequest(List<User> userList, int from, int size) {
        log.info("Repository answered {}, forming answer to controller.", userList);
        if (userList.size() < from) {
            log.info("Value 'from'({}) above result list size({}). Returning empty list.", from, userList.size());
            return new ArrayList<>();
        }
        userList = userList.subList(from, userList.size());
        if (userList.size() <= size) {
            return userList.stream()
                    .map(Mapper::convertUserToDto)
                    .collect(Collectors.toList());
        } else {
            return userList.subList(0, size).stream()
                    .map(Mapper::convertUserToDto)
                    .collect(Collectors.toList());
        }
    }
}
