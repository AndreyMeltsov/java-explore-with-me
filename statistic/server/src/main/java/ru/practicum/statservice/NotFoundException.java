package ru.practicum.statservice;

public class NotFoundException extends NullPointerException {
    public NotFoundException(String message) {
        super(message);
    }
}
