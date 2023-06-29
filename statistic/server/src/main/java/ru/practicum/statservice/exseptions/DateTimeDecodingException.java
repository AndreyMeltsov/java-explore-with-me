package ru.practicum.statservice.exseptions;

public class DateTimeDecodingException extends RuntimeException {
    public DateTimeDecodingException(String message) {
        super(message);
    }
}
