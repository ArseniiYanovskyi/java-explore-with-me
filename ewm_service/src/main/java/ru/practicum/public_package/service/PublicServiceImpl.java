package ru.practicum.public_package.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dao.CategoryRepository;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.exception.NotFoundException;
import ru.practicum.public_package.service.utils.PublicServiceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicServiceImpl implements PublicService{
    private final PublicServiceUtils utils;
    private final CategoryRepository categoryRepository;
    @Override
    public List<CategoryDto> getCategoriesList(int from, int size) {
        log.info("Sending to repository request to get categories list.");
        List<Category> categoryList = categoryRepository.findAll();
        if (categoryList.size() < from) {
            log.info("Value 'from'({}) above result list size({}). Returning empty list.", from, categoryList.size());
            return new ArrayList<>();
        }
        categoryList = categoryList.subList(from, categoryList.size());
        if (categoryList.size() <= size) {
            return categoryList.stream()
                    .map(utils::convertCategoryToDto)
                    .collect(Collectors.toList());
        } else {
            return categoryList.subList(0, size-1).stream()
                    .map(utils::convertCategoryToDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public CategoryDto getCategoryById(long categoryId) {
        return utils.convertCategoryToDto(categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id " + categoryId + " does not present in repository.")));
    }
}
