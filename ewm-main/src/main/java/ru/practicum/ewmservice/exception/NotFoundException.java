package ru.practicum.ewmservice.exception;

public class NotFoundException extends NullPointerException {
    public NotFoundException(String message) {
        super(message);
    }
}
