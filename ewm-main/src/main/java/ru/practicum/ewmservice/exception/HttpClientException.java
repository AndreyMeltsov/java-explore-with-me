package ru.practicum.ewmservice.exception;

public class HttpClientException extends RuntimeException {
    public HttpClientException(String message) {
        super(message);
    }
}
