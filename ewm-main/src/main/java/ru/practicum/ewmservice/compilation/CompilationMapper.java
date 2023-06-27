package ru.practicum.ewmservice.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.event.EventMapper;
import ru.practicum.ewmservice.event.services.StatisticService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;
    private final StatisticService statisticService;

    public CompilationDto mapToDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(compilation.getEvents().stream()
                        .map(e -> eventMapper.mapToShortDto(e, statisticService.getViews(e)))
                        .collect(Collectors.toList()))
                .build();
    }

    public List<CompilationDto> mapToDto(List<Compilation> compilations) {
        return compilations.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}
