package ru.practicum.ewmservice.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.compilation.dto.NewCompilationDto;
import ru.practicum.ewmservice.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewmservice.event.Event;
import ru.practicum.ewmservice.event.EventRepository;
import ru.practicum.ewmservice.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        List<Event> events = new ArrayList<>();
        if (compilationDto.getEvents() != null) {
            events = eventRepository.findAllById(compilationDto.getEvents());
        }
        Compilation newCompilation = Compilation.builder()
                .events(events)
                .pinned(compilationDto.isPinned())
                .title(compilationDto.getTitle())
                .build();
        CompilationDto savedCompilation = compilationMapper.mapToDto(compilationRepository.save(newCompilation));
        log.info("Compilation was added: {}", savedCompilation);
        return savedCompilation;
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationDto) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Compilation with such id wasn't found"));

        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }
        if (compilationDto.getTitle() != null) {
            compilation.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getEvents() != null) {
            compilation.setEvents(compilationDto.getEvents().stream()
                    .map(x -> eventRepository.findById(x)
                            .orElseThrow(() -> new NotFoundException("Event with id =" + x + " wasn't found")))
                    .collect(Collectors.toList()));
        }
        compilationRepository.save(compilation);
        CompilationDto updatedCompilation = compilationMapper.mapToDto(compilation);
        log.info("Compilation was updated: {}", updatedCompilation);
        return updatedCompilation;
    }

    @Override
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with such id wasn't found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageRequest) {
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest);
        } else {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        }
        log.info("{} events was found in DB: {}", compilations.size(), compilationMapper.mapToDto(compilations));
        return compilationMapper.mapToDto(compilations);
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Compilation with such id wasn't found"));
        CompilationDto compilationDto = compilationMapper.mapToDto(compilation);
        log.info("Compilation was found in DB: {}", compilationDto);
        return compilationDto;
    }
}
