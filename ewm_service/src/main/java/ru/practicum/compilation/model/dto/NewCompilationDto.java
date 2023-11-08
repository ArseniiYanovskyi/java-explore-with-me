package ru.practicum.compilation.model.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class NewCompilationDto {
    private List<Long> events;
    private Boolean pinned;
    @NotNull
    @NotBlank
    @Length(max = 50)
    private String title;
}
