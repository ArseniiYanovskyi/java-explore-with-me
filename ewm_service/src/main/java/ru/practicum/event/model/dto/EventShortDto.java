package ru.practicum.event.model.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.users.model.dto.UserShortDto;

@Data
@Builder
public class EventShortDto {
    private long id;
    private String annotation;
    private String title;
    private CategoryDto category;
    private UserShortDto initiator;
    private String eventDate;
    private Boolean paid;
    private Integer confirmedRequests;
    private Long views;
}
