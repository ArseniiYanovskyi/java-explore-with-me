package ru.practicum.event.model.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.category.model.dto.CategoryDto;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.State;
import ru.practicum.users.model.dto.UserShortDto;

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
