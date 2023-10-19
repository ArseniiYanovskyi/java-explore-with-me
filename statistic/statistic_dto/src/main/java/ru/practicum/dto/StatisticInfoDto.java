package ru.practicum.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class StatisticInfoDto {
    String app;
    String uri;
    String ip;
    String timestamp;

    @Override
    public String toString() {
        return "Uri: " + uri +
                ". Of app: " + app +
                ". Requested by ip: " + ip +
                ". Timestamp: " + timestamp + ".";
    }
}
