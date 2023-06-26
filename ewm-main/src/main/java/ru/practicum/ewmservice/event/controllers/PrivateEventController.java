package ru.practicum.ewmservice.event.controllers;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.event.Event;
import ru.practicum.ewmservice.event.dto.EventFullDto;
import ru.practicum.ewmservice.event.dto.EventShortDto;
import ru.practicum.ewmservice.event.dto.NewEventDto;
import ru.practicum.ewmservice.event.dto.UpdateEventRequest;
import ru.practicum.ewmservice.event.services.EventService;
import ru.practicum.ewmservice.request.dto.EventRequestStatusUpdateDto;
import ru.practicum.ewmservice.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewmservice.request.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@AllArgsConstructor
public class PrivateEventController {
    private final EventService eventService;
    private final RequestService requestService;

    @GetMapping
    public List<EventShortDto> getEvents(@PathVariable Long userId,
                                         @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") Integer from,
                                         @Positive @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Pageable pageRequest = PageRequest.of(from / size, size);
        return eventService.getEventsByUserId(userId, pageRequest);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Event createEvent(@PathVariable Long userId,
                             @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable Long userId,
                                     @PathVariable Long eventId) {
        return eventService.getEventByIdAndUserId(eventId, userId);
    }

    @PatchMapping("/{eventId}")
    public Event updateEventByUser(@PathVariable Long userId,
                                   @PathVariable Long eventId,
                                   @Valid @RequestBody UpdateEventRequest updatedEvent) {
        return eventService.updateEventByUser(userId, eventId, updatedEvent);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable Long userId,
                                                     @PathVariable Long eventId) {
        return requestService.getRequestsByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequests(@PathVariable Long userId,
                                                         @PathVariable Long eventId,
                                                         @RequestBody EventRequestStatusUpdateDto updatedRequest) {
        return requestService.updateRequests(userId, eventId, updatedRequest);
    }

}
