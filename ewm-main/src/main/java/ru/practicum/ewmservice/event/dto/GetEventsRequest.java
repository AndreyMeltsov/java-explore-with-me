package ru.practicum.ewmservice.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewmservice.event.DateFormatter;
import ru.practicum.ewmservice.event.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetEventsRequest {
    private List<Long> userIds;
    private List<EventState> states;
    private List<Long> categoryIds;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Pageable pageRequest;

    public static GetEventsRequest of(List<Long> users,
                                      List<String> states,
                                      List<Long> categories,
                                      String rangeStart,
                                      String rangeEnd,
                                      Integer from,
                                      Integer size) {
        return GetEventsRequest.builder()
                .userIds(users)
                .states(states != null ? states.stream().map(String::toUpperCase)
                        .map(EventState::valueOf)
                        .collect(Collectors.toList()) : null)
                .categoryIds(categories)
                .rangeStart(rangeStart != null ? LocalDateTime.parse(rangeStart, DateFormatter.FORMATTER) : null)
                .rangeEnd(rangeEnd != null ? LocalDateTime.parse(rangeEnd, DateFormatter.FORMATTER) : null)
                .pageRequest(PageRequest.of(from / size, size))
                .build();
    }
}
