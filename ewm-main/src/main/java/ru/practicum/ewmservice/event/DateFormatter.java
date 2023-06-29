package ru.practicum.ewmservice.event;

import java.time.format.DateTimeFormatter;

public class DateFormatter {

    private DateFormatter() {
        throw new IllegalStateException("Utility class");
    }

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
