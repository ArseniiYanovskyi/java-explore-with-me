package ru.practicum.model.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.model.request.Status;

@Data
@Builder
public class ParticipationRequestDto {
    private long id;
    private String created;
    private long event;
    private long requester;
    private Status status;
}
