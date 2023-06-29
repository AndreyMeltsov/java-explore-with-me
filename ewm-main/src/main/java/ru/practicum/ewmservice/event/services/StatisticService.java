package ru.practicum.ewmservice.event.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.event.Event;
import ru.practicum.ewmservice.event.EventState;
import ru.practicum.ewmservice.exception.HttpClientException;
import ru.practicum.statclient.StatClient;
import ru.practicum.statdto.HitDto;
import ru.practicum.statdto.ViewStatsDto;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewmservice.event.DateFormatter.FORMATTER;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticService {
    private final StatClient client;

    public void sendStatistic(HttpServletRequest request) {
        HitDto hitDto = HitDto.builder()
                .app("ewm-main")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
        log.info("Client will send next info to statistic server: {}", hitDto);
        client.post(hitDto);
    }

    public Long getViews(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            return 0L;
        }
        List<ViewStatsDto> viewStats;
        try {
            viewStats = client.get(event.getPublishedOn(),
                    LocalDateTime.now().plusSeconds(1L),
                    new String[]{"/events/" + event.getId()},
                    true);
            log.info("Trying to get view statistic. Received response is: {}", viewStats);
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            throw new HttpClientException(e.getMessage());
        }
        return viewStats.stream().map(ViewStatsDto::getHits).findFirst().orElse(0L);
    }
}
