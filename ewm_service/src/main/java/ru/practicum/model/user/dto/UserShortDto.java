package ru.practicum.model.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserShortDto {
    private long id;
    private String name;
}
