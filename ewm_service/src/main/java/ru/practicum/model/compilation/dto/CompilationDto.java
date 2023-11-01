package ru.practicum.model.compilation.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.model.event.dto.EventShortDto;

import java.util.List;

@Data
@Builder
public class CompilationDto {
    private long id;
    private List<EventShortDto> events;
    private Boolean pinned;
    private String title;
}
