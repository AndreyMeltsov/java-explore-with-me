package ru.practicum.ewmservice.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.ewmservice.category.Category;
import ru.practicum.ewmservice.event.Location;
import ru.practicum.ewmservice.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {
    private Long id;

    private String annotation;

    private Category category;

    private int confirmedRequests;

    private LocalDateTime createdOn;

    private LocalDateTime publishedOn;

    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private UserShortDto initiator;

    private Location location;

    private boolean paid;

    private int participantLimit;

    private boolean requestModeration;

    private String state;

    private String title;

    private Long views;
}
