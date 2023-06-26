package ru.practicum.statservice.exseptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<ApiError> handleValidation(final IllegalArgumentExcepton e) {
        log.error("400 {}", e.getMessage(), e);
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .errors(List.of(e.getClass().getName()))
                .message(e.getLocalizedMessage())
                .reason("For the requested operation the conditions are not met.")
                .status(HttpStatus.BAD_REQUEST)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);

    }
}
