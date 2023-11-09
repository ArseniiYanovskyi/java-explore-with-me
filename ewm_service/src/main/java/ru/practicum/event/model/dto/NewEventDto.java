package ru.practicum.event.model.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.event.model.Location;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class NewEventDto {
    @NotNull
    @NotBlank
    @Length(min = 20, max = 2000)
    private String annotation;
    @NotNull
    @Min(1L)
    private Long category;
    @NotNull
    @NotBlank
    @Length(min = 20, max = 7000)
    private String description;
    private String eventDate;
    @NotNull
    private Location location;
    private Boolean paid;
    private Boolean requestModeration;
    private Integer participantLimit;
    @NotNull
    @NotBlank
    @Length(min = 3, max = 120)
    private String title;
}
