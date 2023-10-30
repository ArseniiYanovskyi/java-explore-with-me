package ru.practicum.model.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.user.dto.UserShortDto;

@Data
@Builder
public class EventShortDto {
    private long id;
    private String annotation;
    private String title;
    private CategoryDto categoryDto;
    private UserShortDto initiator;
    private String eventDate;
    private boolean paid;
    private int confirmedRequests;
    private int views;
}
