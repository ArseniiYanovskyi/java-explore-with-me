package ru.practicum.model.compilation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NewCompilationDto {
    private List<Integer> events;
    private Boolean pinned;
    private String title;
}
