package ru.practicum.ewm;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.HitDto;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping
@Slf4j
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public void createHit(@RequestBody HitDto hitDto) {
        log.info("Мы хотим посмотреть что приходит в теле запроса: {}", hitDto);
        statsService.createHit(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam String start,
                                       @RequestParam String end,
                                       @RequestParam(required = false) String[] uris,
                                       @RequestParam(defaultValue = "false") Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime from = LocalDateTime.parse(start, formatter);
        LocalDateTime to = LocalDateTime.parse(end, formatter);

        return statsService.getStats(from, to, uris, unique);
    }
}
