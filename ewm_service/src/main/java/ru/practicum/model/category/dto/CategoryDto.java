package ru.practicum.model.category.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {
    private long id;
    private String name;
}