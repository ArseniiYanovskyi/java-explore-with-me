package ru.practicum.request.model.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.request.model.Status;

@Data
@Builder
public class ParticipationRequestDto {
    private long id;
    private String created;
    private long event;
    private long requester;
    private Status status;
}
