package ru.practicum.statservice;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.statdto.HitDto;
import ru.practicum.statdto.ViewStatsDto;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping
public class StatsController {
    private final StatsService statsService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public void createHit(@RequestBody HitDto hitDto) {
        statsService.createHit(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam String start,
                                       @RequestParam String end,
                                       @RequestParam(required = false) String[] uris,
                                       @RequestParam(defaultValue = "false") Boolean unique) {
        return statsService.getStats(start, end, uris, unique);
    }
}
