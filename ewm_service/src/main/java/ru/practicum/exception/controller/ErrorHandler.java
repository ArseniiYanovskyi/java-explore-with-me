package ru.practicum.exception.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.model.ApiError;
import ru.practicum.exception.model.BadRequestException;
import ru.practicum.exception.model.ConflictRequestException;
import ru.practicum.exception.model.NotFoundException;
import ru.practicum.utils.Mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError errorResponseNotFound(NotFoundException e) {
        log.debug("Returning {} answer with message: {}", "NOT_FOUND", e.getMessage());
        List<String> errors = new ArrayList<>();
        for (StackTraceElement ste : e.getStackTrace()) {
            errors.add(ste.toString());
        }
        return ApiError.builder()
                .errors(errors)
                .message(e.getMessage())
                .reason(e.getReason())
                .timestamp(LocalDateTime.now().format(Mapper.formatter))
                .status(HttpStatus.NOT_FOUND.toString())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError errorResponseIncorrectRequest(ConflictRequestException e) {
        log.debug("Returning {} answer with message: {}", "CONFLICT", e.getMessage());
        List<String> errors = new ArrayList<>();
        for (StackTraceElement ste : e.getStackTrace()) {
            errors.add(ste.toString());
        }
        return ApiError.builder()
                .errors(errors)
                .message(e.getMessage())
                .reason(e.getReason())
                .timestamp(LocalDateTime.now().format(Mapper.formatter))
                .status(HttpStatus.CONFLICT.toString())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError errorResponseIncorrectRequest(BadRequestException e) {
        log.debug("Returning {} answer with message: {}", "BAD_REQUEST", e.getMessage());
        List<String> errors = new ArrayList<>();
        for (StackTraceElement ste : e.getStackTrace()) {
            errors.add(ste.toString());
        }
        return ApiError.builder()
                .errors(errors)
                .message(e.getMessage())
                .reason(e.getReason())
                .timestamp(LocalDateTime.now().format(Mapper.formatter))
                .status(HttpStatus.BAD_REQUEST.toString())
                .build();
    }
}
