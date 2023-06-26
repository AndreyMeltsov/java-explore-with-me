package ru.practicum.ewmservice.event.services;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewmservice.event.Event;
import ru.practicum.ewmservice.event.dto.EventFullDto;
import ru.practicum.ewmservice.event.dto.EventShortDto;
import ru.practicum.ewmservice.event.dto.GetEventsByQueryRequest;
import ru.practicum.ewmservice.event.dto.GetEventsRequest;
import ru.practicum.ewmservice.event.dto.NewEventDto;
import ru.practicum.ewmservice.event.dto.UpdateEventRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {
    List<EventShortDto> getEventsByUserId(Long userId, Pageable pageRequest);

    Event createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);

    EventFullDto getEventByIdAndUserId(Long eventId, Long userId);

    Event updateEventByUser(Long userId, Long eventId, UpdateEventRequest updatedEvent);

    Event updateEventByAdmin(Long eventId, UpdateEventRequest updatedEvent);

    List<EventFullDto> getEvents(GetEventsRequest request);

    List<EventShortDto> getEventsByQuery(GetEventsByQueryRequest queryRequest, HttpServletRequest request);
}
