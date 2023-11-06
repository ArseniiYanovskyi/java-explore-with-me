package ru.practicum.model.event.dto;

import lombok.Data;
import ru.practicum.model.event.Location;

@Data
public class NewEventDto {
    private String annotation;
    private Long category;
    private String description;
    private String eventDate;
    private Location location;
    private Boolean paid;
    private Boolean requestModeration;
    private Integer participantLimit;
    private String title;
}
