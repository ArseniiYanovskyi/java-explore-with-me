package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/admin/categories")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid CategoryDto categoryDto) {
        log.info("Received request to add new category of event: {}.", categoryDto.getName());
        return categoryService.addCategory(categoryDto);
    }

    @PatchMapping("/admin/categories/{catId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable long catId, @RequestBody @Valid CategoryDto categoryDto) {
        log.info("Received request to update category with id {} to a new name: {}.", catId, categoryDto.getName());
        return categoryService.updateCategory(catId, categoryDto);
    }

    @GetMapping("/categories")
    @ResponseStatus(code = HttpStatus.OK)
    public List<CategoryDto> getCategoriesList(@RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get categories list from: {}, size: {}.", from, size);
        return categoryService.getCategoriesList(from, size);
    }

    @GetMapping("/categories/{catId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CategoryDto getCategoryById(@PathVariable long catId) {
        log.info("Received request to get category with id: {}.", catId);
        return categoryService.getCategoryById(catId);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable(value = "catId") long catId) {
        log.info("Received request to delete category with id {}.", catId);
        categoryService.deleteCategory(catId);
    }
}
