package ru.practicum.statservice.exseptions;

public class IllegalArgumentExcepton extends RuntimeException {
    public IllegalArgumentExcepton(String message) {
        super(message);
    }
}
