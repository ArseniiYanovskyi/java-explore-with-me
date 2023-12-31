package ru.practicum.event.model;

import java.util.Optional;

public enum State {
    PENDING, PUBLISHED, CANCELED;

    public static Optional<State> parseState(String query) {
        return Optional.of(State.valueOf(query));
    }
}
