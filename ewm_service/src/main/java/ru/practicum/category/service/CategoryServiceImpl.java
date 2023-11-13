package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.category.model.Category;
import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.exception.model.ConflictRequestException;
import ru.practicum.exception.model.NotFoundException;
import ru.practicum.utils.Mapper;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto addCategory(CategoryDto categoryDto) {
        Optional<Category> optionalCategory = categoryRepository.findByName(categoryDto.getName());
        if (optionalCategory.isPresent()) {
            log.info("Category with this name already present in repository.");
            throw new ConflictRequestException("Category with this name already present in repository.");
        }

        log.info("Sending to repository request to add new category: {}.", categoryDto.getName());
        Category category = categoryRepository.save(Mapper.convertCategoryFromDto(categoryDto));
        return Mapper.convertCategoryToDto(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long categoryId, CategoryDto categoryDto) {
        Optional<Category> optionalCategory = categoryRepository.findByName(categoryDto.getName());
        if (optionalCategory.isPresent() && optionalCategory.get().getId() != categoryId) {
            log.info("Category with this name already present in repository.");
            throw new ConflictRequestException("Category with this name already present in repository.");
        }

        log.info("Sending to repository request to update category with id {} to: {}.", categoryId, categoryDto.getName());
        Category category = categoryRepository.save(Mapper.convertCategoryFromDto(categoryDto));
        return Mapper.convertCategoryToDto(category);
    }

    @Override
    @Transactional
    public List<CategoryDto> getCategoriesList(int from, int size) {
        log.info("Sending to repository request to get categories list.");
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(Mapper::convertCategoryToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDto getCategoryById(long categoryId) {
        return Mapper.convertCategoryToDto(categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id " + categoryId + " does not present in repository.")));
    }

    @Override
    @Transactional
    public void deleteCategory(long categoryId) {
        if (!eventRepository.findAllByCategoryId(categoryId).isEmpty()) {
            throw new ConflictRequestException("In repository presents events connected to this category.");
        }
        log.info("Sending to repository request to delete category with id {}.", categoryId);
        categoryRepository.deleteById(categoryId);
    }
}
