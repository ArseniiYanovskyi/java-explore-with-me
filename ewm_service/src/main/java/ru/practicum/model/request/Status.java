package ru.practicum.model.request;

import java.util.Optional;

public enum Status {
    PENDING, CONFIRMED, REJECTED;
    public Optional<Status> parseStatus(String query) {
        return Optional.of(Status.valueOf(query));
    }
}
