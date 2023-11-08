package ru.practicum.category.model.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class CategoryDto {
    private long id;
    @NotNull
    @NotBlank
    @Length(min = 2, max = 50)
    private String name;
}
