package ru.practicum.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.CONFLICT)
public class IncorrectRequestException extends ResponseStatusException {
    public IncorrectRequestException(String reason) {
        super(HttpStatus.CONFLICT, reason);
    }
}
