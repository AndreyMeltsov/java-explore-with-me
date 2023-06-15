package ru.practicum.ewm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {
    private final StatsRepository statsRepository;

    @Transactional
    public void createHit(HitDto hitDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Hit hit = Hit.builder()
                .app(hitDto.getApp())
                .ip(hitDto.getIp())
                .uri(hitDto.getUri())
                .timestamp(LocalDateTime.parse(hitDto.getTimestamp(), formatter))
                .build();
        hit = statsRepository.save(hit);
        log.info("Statistic was added: {}", hit);
    }

    public List<ViewStatsDto> getStats(LocalDateTime from, LocalDateTime to,
                                       String[] uris, boolean unique) {
        if (uris == null) {
            return unique ? statsRepository.getUniqueIdStats(from, to) :
                    statsRepository.getAllIdStats(from, to);
        } else {
            return unique ? statsRepository.getUniqueIdStatsByUris(from, to, uris) :
                    statsRepository.getAllIdStatsByUris(from, to, uris);
        }
    }
}
