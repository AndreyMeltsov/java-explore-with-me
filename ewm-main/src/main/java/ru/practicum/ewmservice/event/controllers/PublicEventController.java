package ru.practicum.ewmservice.event.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.event.dto.EventFullDto;
import ru.practicum.ewmservice.event.dto.EventShortDto;
import ru.practicum.ewmservice.event.dto.GetEventsByQueryRequest;
import ru.practicum.ewmservice.event.services.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/events")
@AllArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(value = "text", required = false) String text,
                                         @RequestParam(value = "categories", required = false) List<Long> categories,
                                         @RequestParam(value = "paid", required = false) Boolean paid,
                                         @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                         @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                         @RequestParam(value = "onlyAvailable", defaultValue = "false")
                                         boolean onlyAvailable,
                                         @RequestParam(value = "sort", required = false) String sort,
                                         @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
                                         @Positive @RequestParam(value = "size", defaultValue = "10") int size,
                                         HttpServletRequest request) {

        return eventService.getEventsByQuery(GetEventsByQueryRequest.of(text, categories, paid, rangeStart,
                rangeEnd, onlyAvailable, sort, from, size), request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable Long id, HttpServletRequest request) {
        return eventService.getEventById(id, request);
    }
}
