package ru.practicum.event.model.dto;

import java.util.Optional;

public enum PublicSearchEventSort {
    EVENT_DATE, VIEWS;

    public static Optional<PublicSearchEventSort> parseSearchSort(String query) {
        return Optional.of(PublicSearchEventSort.valueOf(query));
    }
}
