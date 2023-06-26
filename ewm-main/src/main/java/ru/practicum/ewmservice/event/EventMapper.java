package ru.practicum.ewmservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import ru.practicum.statdto.ViewStatsDto;
import ru.practicum.statclient.StatClient;
import ru.practicum.ewmservice.event.dto.EventFullDto;
import ru.practicum.ewmservice.event.dto.EventShortDto;
import ru.practicum.ewmservice.exception.HttpClientException;
import ru.practicum.ewmservice.request.RequestRepository;
import ru.practicum.ewmservice.request.RequestStatus;
import ru.practicum.ewmservice.user.UserMapper;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ComponentScan(basePackages = {"ru.practicum.statclient", "ru.practicum.ewmservice"})
@RequiredArgsConstructor
@Slf4j
public class EventMapper {
    private final UserMapper userMapper;
    private final StatClient client;
    private final RequestRepository requestRepository;

    public EventShortDto mapToShortDto(Event event) {
        Long views = getViews(event);
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(event.getCategory())
                .confirmedRequests(requestRepository.findCountRequestsByEventIdAndStatus(event.getId(),
                        RequestStatus.CONFIRMED))
                .eventDate(event.getEventDate())
                .initiator(userMapper.toUserShortDto(event.getInitiator()))
                .paid(event.isPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public EventShortDto fullToShortDto(EventFullDto eventFullDto) {
        return EventShortDto.builder()
                .id(eventFullDto.getId())
                .annotation(eventFullDto.getAnnotation())
                .category(eventFullDto.getCategory())
                .confirmedRequests(eventFullDto.getConfirmedRequests())
                .eventDate(eventFullDto.getEventDate())
                .initiator(eventFullDto.getInitiator())
                .paid(eventFullDto.isPaid())
                .title(eventFullDto.getTitle())
                .views(eventFullDto.getViews())
                .build();
    }

    public EventFullDto mapToFullDto(Event event) {
        Long views = getViews(event);
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(event.getCategory())
                .confirmedRequests(requestRepository.findCountRequestsByEventIdAndStatus(event.getId(),
                        RequestStatus.CONFIRMED))
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(userMapper.toUserShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.isRequestModeration())
                .state(event.getState().name())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public List<EventShortDto> mapToShortDto(List<Event> events) {
        if (events == null) {
            return Collections.emptyList();
        }
        return events.stream().map(this::mapToShortDto).collect(Collectors.toList());
    }

    public List<EventShortDto> fullToShortDto(List<EventFullDto> events) {
        if (events == null) {
            return Collections.emptyList();
        }
        return events.stream().map(this::fullToShortDto).collect(Collectors.toList());
    }

    public List<EventFullDto> mapToFullDto(List<Event> events) {
        if (events == null) {
            return Collections.emptyList();
        }
        return events.stream().map(this::mapToFullDto).collect(Collectors.toList());
    }

    private Long getViews(Event event) {
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
