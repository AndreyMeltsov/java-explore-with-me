package ru.practicum.ewmservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;


@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<ApiError> handleValidation(final ValidationException e) {
        log.error("400 {}", e.getMessage(), e);
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .errors(List.of(e.getClass().getName()))
                .message(e.getLocalizedMessage())
                .reason("For the requested operation the conditions are not met.")
                .status(HttpStatus.BAD_REQUEST)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);

    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleNotFound(final NotFoundException e) {
        log.error("404 {}", e.getMessage(), e);
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .errors(List.of(e.getClass().getName()))
                .message(e.getLocalizedMessage())
                .reason("The required object was not found.")
                .status(HttpStatus.NOT_FOUND)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }


    @ExceptionHandler
    public ResponseEntity<ApiError> handleAllIllegal(final IllegalArgumentException e) {
        log.error("500 {}", e.getMessage(), e);
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .errors(List.of(e.getClass().getName()))
                .message(e.getLocalizedMessage())
                .reason("Error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        return new ResponseEntity<>(apiError,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleConflict(final ConflictException e) {
        log.error("409 {}", e.getMessage(), e);
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .errors(List.of(e.getClass().getName()))
                .message(e.getLocalizedMessage())
                .reason("Integrity constraint has been violated")
                .status(HttpStatus.CONFLICT)
                .build();
        return new ResponseEntity<>(apiError,
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler({javax.validation.ValidationException.class,
            MethodArgumentNotValidException.class})
    public ResponseEntity<ApiError> constraintViolationHandle(final RuntimeException e) {
        log.error("409 {}", e.getMessage(), e);
        ApiError apiError = new ApiError.ApiErrorBuilder()
                .errors(List.of(e.getClass().getName()))
                .message(e.getLocalizedMessage())
                .reason("Constraint violation has been occurred")
                .status(HttpStatus.CONFLICT)
                .build();
        return new ResponseEntity<>(apiError,
                HttpStatus.CONFLICT);
    }
}
