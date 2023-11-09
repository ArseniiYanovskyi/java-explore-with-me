package ru.practicum.request.model.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.request.model.Status;

import java.util.List;

@Data
@Builder
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private Status status;
}
