package ru.practicum.ewmservice.event.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.event.Event;
import ru.practicum.ewmservice.event.dto.EventFullDto;
import ru.practicum.ewmservice.event.dto.GetEventsRequest;
import ru.practicum.ewmservice.event.dto.UpdateEventRequest;
import ru.practicum.ewmservice.event.services.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@AllArgsConstructor
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam(value = "users", required = false) List<Long> users,
                                        @RequestParam(value = "states", required = false) List<String> states,
                                        @RequestParam(value = "categories", required = false) List<Long> categories,
                                        @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                        @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                        @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") Integer from,
                                        @Positive @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return eventService.getEvents(GetEventsRequest.of(users, states, categories, rangeStart, rangeEnd, from, size));
    }

    @PatchMapping("/{eventId}")
    public Event updateEventByAdmin(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventRequest updatedEvent) {
        return eventService.updateEventByAdmin(eventId, updatedEvent);
    }
}
