package ru.practicum.model.event.dto;

import lombok.Data;
import ru.practicum.model.event.Location;

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
