package ru.practicum.ewmservice.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.ewmservice.event.dto.EventShortDto;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    private Long id;

    private List<EventShortDto> events;

    private boolean pinned;

    private String title;
}
