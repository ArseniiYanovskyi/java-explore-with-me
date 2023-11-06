package ru.practicum.model.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.event.Location;
import ru.practicum.model.event.State;
import ru.practicum.model.user.dto.UserShortDto;

@Data
@Builder
public class EventFullDto {
    private long id;
    private String annotation;
    private String description;
    private String title;
    private CategoryDto category;
    private UserShortDto initiator;
    private String createdOn;
    private String publishedOn;
    private String eventDate;
    private Location location;
    private boolean paid;
    private int participantLimit;
    private int confirmedRequests;
    private boolean requestModeration;
    private State state;
    private long views;
}
