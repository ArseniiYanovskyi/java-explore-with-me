package ru.practicum.users.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserShortDto {
    private long id;
    private String name;
}
