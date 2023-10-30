package ru.practicum.model.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.model.request.Status;

import java.util.List;

@Data
@Builder
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private Status status;
}
