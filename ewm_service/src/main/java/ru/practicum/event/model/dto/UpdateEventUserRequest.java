package ru.practicum.event.model.dto;

import lombok.Data;
import ru.practicum.event.model.Location;

@Data
public class UpdateEventUserRequest {
    private String annotation;
    private String description;
    private String title;
    private Long category;
    private String eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;
}
