package ru.practicum.ewmservice.compilation;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewmservice.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.compilation.dto.NewCompilationDto;
import ru.practicum.ewmservice.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto compilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationDto);

    void deleteCompilation(Long compId);

    List<CompilationDto> getCompilations(Boolean pinned, Pageable pageRequest);

    CompilationDto getCompilationById(Long compId);
}
