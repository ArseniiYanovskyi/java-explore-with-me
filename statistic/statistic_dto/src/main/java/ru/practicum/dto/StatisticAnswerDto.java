package ru.practicum.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticAnswerDto {
    String app;
    String uri;
    int hits;
}
