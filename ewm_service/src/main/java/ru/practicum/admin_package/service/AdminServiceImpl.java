package ru.practicum.admin_package.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dao.CategoryRepository;
import ru.practicum.dao.UserRepository;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.user.User;
import ru.practicum.model.user.dto.UserDto;
import ru.practicum.admin_package.service.utils.AdminServiceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final AdminServiceUtils utils;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto addCategory(CategoryDto categoryDto) {
        log.info("Sending to repository request to add new category: {}.", categoryDto.getName());
        Category category = categoryRepository.save(utils.convertCategoryFromDto(categoryDto));
        return utils.convertCategoryToDto(category);
    }

    @Override
    public CategoryDto updateCategory(long categoryId, CategoryDto categoryDto) {
        log.info("Sending to repository request to update category with id {} to: {}.", categoryId, categoryDto.getName());
        Category category = categoryRepository.save(utils.convertCategoryFromDto(categoryDto));
        return utils.convertCategoryToDto(category);
    }

    @Override
    public void deleteCategory(long categoryId) {
        log.info("Sending to repository request to delete category with id {}.", categoryId);
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        log.info("Sending to repository request to add new user. name: {}. email: {}.", userDto.getName(), userDto.getEmail());
        User user = userRepository.save(utils.convertUserFromDto(userDto));
        return utils.convertUserToDto(user);
    }

    @Override
    public List<UserDto> getUsersList(List<Long> ids, int from, int size) {
        log.info("Sending to repository request to get user list. ids: {}", ids);
        if (ids.isEmpty()) {
            return formResultForUserListRequest(userRepository.findAll(), from, size);

        }
        return formResultForUserListRequest(userRepository.findByIdIn(ids), from, size);
    }

    @Override
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
                    .map(utils::convertUserToDto)
                    .collect(Collectors.toList());
        } else {
            return userList.subList(0, size-1).stream()
                    .map(utils::convertUserToDto)
                    .collect(Collectors.toList());
        }
    }
}
