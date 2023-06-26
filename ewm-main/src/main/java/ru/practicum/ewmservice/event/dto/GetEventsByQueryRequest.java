package ru.practicum.ewmservice.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewmservice.event.DateFormatter.FORMATTER;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetEventsByQueryRequest {
    private String query;
    private List<Long> categoryIds;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private boolean onlyAvailable;
    private Sort sort;
    private int from;
    private int size;

    public static GetEventsByQueryRequest of(String text,
                                             List<Long> categories,
                                             Boolean paid,
                                             String rangeStart,
                                             String rangeEnd,
                                             boolean onlyAvailable,
                                             String sort,
                                             int from,
                                             int size) {
        return GetEventsByQueryRequest.builder()
                .query(text)
                .categoryIds(categories)
                .paid(paid)
                .rangeStart(rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : LocalDateTime.now())
                .rangeEnd(rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : LocalDateTime.now().plusYears(10))
                .onlyAvailable(onlyAvailable)
                .sort(sort != null ? Sort.valueOf(sort.toUpperCase()) : null)
                .from(from)
                .size(size)
                .build();
    }

    public enum Sort {EVENT_DATE, VIEWS}

}
