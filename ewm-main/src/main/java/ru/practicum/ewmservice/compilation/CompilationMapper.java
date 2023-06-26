package ru.practicum.ewmservice.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.event.EventMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;

    public CompilationDto mapToDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(eventMapper.mapToShortDto(compilation.getEvents()))
                .build();
    }

    public List<CompilationDto> mapToDto(List<Compilation> compilations) {
        return compilations.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}
