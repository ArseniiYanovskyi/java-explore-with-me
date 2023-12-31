package ru.practicum.exception.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictRequestException extends ResponseStatusException {
    public ConflictRequestException(String reason) {
        super(HttpStatus.CONFLICT, reason);
    }
}
