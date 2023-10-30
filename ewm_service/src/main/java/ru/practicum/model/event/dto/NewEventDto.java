package ru.practicum.model.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.model.event.Location;

@Data
public class NewEventDto {
    private String annotation;
    private long category;
    private String description;
    private String eventDate;
    private Location location;
    private boolean paid;
    private boolean requestModeration;
    private int participantLimit;
    private String title;
}
