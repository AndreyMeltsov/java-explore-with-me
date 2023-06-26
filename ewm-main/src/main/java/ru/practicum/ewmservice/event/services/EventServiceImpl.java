package ru.practicum.ewmservice.event.services;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.category.Category;
import ru.practicum.ewmservice.category.CategoryRepository;
import ru.practicum.ewmservice.event.Event;
import ru.practicum.ewmservice.event.EventMapper;
import ru.practicum.ewmservice.event.EventRepository;
import ru.practicum.ewmservice.event.EventState;
import ru.practicum.ewmservice.event.EventStateAction;
import ru.practicum.ewmservice.event.QEvent;
import ru.practicum.ewmservice.event.dto.EventFullDto;
import ru.practicum.ewmservice.event.dto.EventShortDto;
import ru.practicum.ewmservice.event.dto.GetEventsByQueryRequest;
import ru.practicum.ewmservice.event.dto.GetEventsRequest;
import ru.practicum.ewmservice.event.dto.NewEventDto;
import ru.practicum.ewmservice.event.dto.UpdateEventRequest;
import ru.practicum.ewmservice.exception.ConflictException;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.exception.ValidationException;
import ru.practicum.ewmservice.user.User;
import ru.practicum.ewmservice.user.UserRepository;
import ru.practicum.statclient.StatClient;
import ru.practicum.statdto.HitDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewmservice.event.DateFormatter.FORMATTER;
import static ru.practicum.ewmservice.event.dto.GetEventsByQueryRequest.Sort.VIEWS;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatClient client;

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Pageable pageRequest) {
        List<Event> events = eventRepository.findByInitiatorId(userId, pageRequest);
        List<EventShortDto> eventShort = eventMapper.mapToShortDto(events);
        log.info("Events was found in DB: {}", eventShort);
        return eventShort;
    }

    @Transactional
    @Override
    public Event createEvent(Long userId, NewEventDto newEventDto) {
        Category eventCat = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with such id wasn't found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with such id wasn't found"));
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
            throw new ConflictException("Event can't begin earlier than 2 hours later than now");
        }
        Event event = Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(eventCat)
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(newEventDto.getLocation())
                .paid(newEventDto.isPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.isRequestModeration())
                .title(newEventDto.getTitle())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .initiator(user)
                .build();
        Event savedEvent = eventRepository.save(event);
        log.info("Event was added: {}", savedEvent);
        return savedEvent;
    }

    @Override
    public EventFullDto getEventByIdAndUserId(Long eventId, Long userId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id wasn't found"));
        EventFullDto eventFull = eventMapper.mapToFullDto(event);
        log.info("Event was found in DB: {}", eventFull);
        return eventFull;
    }

    @Transactional
    @Override
    public Event updateEventByUser(Long userId, Long eventId, UpdateEventRequest updatedEvent) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with such id wasn't found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User isn't initiator. Only initiators can change event data");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Only pending or canceled event can be changed");
        }
        if (updatedEvent.getEventDate() != null) {
            LocalDateTime updatedEvenDate = LocalDateTime.parse(updatedEvent.getEventDate(), FORMATTER);
            if (updatedEvenDate.isBefore(LocalDateTime.now().plusHours(2L))) {
                throw new ValidationException("Event can't begin earlier than 2 hours later than now");
            }
            event.setEventDate(updatedEvenDate);
        }
        if (updatedEvent.getStateAction() != null) {
            EventStateAction action = EventStateAction.valueOf(updatedEvent.getStateAction().toUpperCase());
            switch (action) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected enum constant: " + action);
            }
        }
        checkFieldsBeforeUpdate(event, updatedEvent);
        Event savedEvent = eventRepository.save(event);
        log.info("Event was updated in DB by initiator. New event is: {}", savedEvent);
        return savedEvent;
    }

    @Override
    public List<EventFullDto> getEvents(GetEventsRequest request) {
        log.info("Попробуем получить мероприятия со следующим фильтром: {}", request);
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();

        BooleanExpression dateTimeCondition = makeDateTimeCondition(request.getRangeStart(), request.getRangeEnd());
        if (dateTimeCondition != null) {
            conditions.add(dateTimeCondition);
        }
        if (request.getUserIds() != null) {
            conditions.add(event.initiator.id.in(request.getUserIds()));
        }
        if (request.getCategoryIds() != null) {
            conditions.add(event.category.id.in(request.getCategoryIds()));
        }
        if (request.getStates() != null) {
            conditions.add(event.state.in(request.getStates()));
        }
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElseThrow(() -> new NotFoundException("At least one condition should be assigned"));

        List<Event> events = eventRepository.findAll(finalCondition, request.getPageRequest()).getContent();
        List<EventFullDto> eventFullDtos = eventMapper.mapToFullDto(events);
        log.info("{} events was found in DB: {}", eventFullDtos.size(), eventFullDtos);
        return eventFullDtos;
    }

    @Transactional
    @Override
    public Event updateEventByAdmin(Long eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with such id wasn't found"));
        if (updateEventRequest.getStateAction() != null) {
            EventStateAction action = EventStateAction.valueOf(updateEventRequest.getStateAction().toUpperCase());
            switch (action) {
                case PUBLISH_EVENT:
                    if (event.getState().equals(EventState.PENDING)
                            && event.getEventDate().isAfter(LocalDateTime.now().plusHours(1L))) {
                        event.setState(EventState.PUBLISHED);
                        event.setPublishedOn(LocalDateTime.now());
                    } else {
                        throw new ConflictException("Only pending event can be published");
                    }
                    break;
                case REJECT_EVENT:
                    if (!event.getState().equals(EventState.PUBLISHED)) {
                        event.setState(EventState.CANCELED);
                    } else {
                        throw new ConflictException("Only published events can be canceled");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected enum constant: " + action);
            }
        }
        if (updateEventRequest.getEventDate() != null) {
            LocalDateTime updatedEventDate = LocalDateTime.parse(updateEventRequest.getEventDate(), FORMATTER);
            if (event.getPublishedOn() == null) {
                if (updatedEventDate.isBefore(LocalDateTime.now().plusHours(2L))) {
                    throw new ValidationException("Unpublished event can't begin earlier than 2 hours later than now");
                }
            } else {
                if (updatedEventDate.isBefore(event.getPublishedOn().plusHours(1L))) {
                    throw new ConflictException("Event can't begin earlier than 1 hours later than published");
                }
                event.setEventDate(updatedEventDate);
            }
        }
        checkFieldsBeforeUpdate(event, updateEventRequest);
        log.info("Event was updated in DB by admin. New event is: {}", event);
        return eventRepository.save(event);
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with such id wasn't found"));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Event with such id neither was published nor canceled");
        }
        EventFullDto eventFull = eventMapper.mapToFullDto(event);
        log.info("Event was found in DB: {}", eventFull);
        sendStatistic(request);
        return eventFull;
    }

    @Override
    public List<EventShortDto> getEventsByQuery(GetEventsByQueryRequest queryRequest, HttpServletRequest request) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();

        BooleanExpression dateTimeCondition = makeDateTimeCondition(queryRequest.getRangeStart(),
                queryRequest.getRangeEnd());
        if (dateTimeCondition != null) {
            conditions.add(dateTimeCondition);
        }
        if (queryRequest.getQuery() != null && !queryRequest.getQuery().isBlank()) {
            conditions.add(event.annotation
                    .likeIgnoreCase("%" + queryRequest.getQuery().toLowerCase().trim() + "%")
                    .or(event.description.likeIgnoreCase("%" + queryRequest.getQuery().toLowerCase().trim() + "%")));
        }
        if (queryRequest.getCategoryIds() != null) {
            conditions.add(event.category.id.in(queryRequest.getCategoryIds()));
        }
        if (queryRequest.getPaid() != null) {
            conditions.add(event.paid.eq(queryRequest.getPaid()));
        }

        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElseThrow(() -> new NotFoundException("At least one condition should be assigned"));

        List<Event> events = new ArrayList<>();
        eventRepository.findAll(finalCondition).forEach(events::add);

        List<EventFullDto> eventFullDtos = eventMapper.mapToFullDto(events);

        if (queryRequest.isOnlyAvailable()) {
            eventFullDtos = eventFullDtos.stream()
                    .filter(e -> e.getParticipantLimit() == 0
                            || e.getConfirmedRequests() < e.getParticipantLimit())
                    .collect(Collectors.toList());
        }

        if (queryRequest.getSort() != null) {
            eventFullDtos = eventFullDtos.stream()
                    .sorted(getComparator(queryRequest.getSort()))
                    .collect(Collectors.toList());
        }

        eventFullDtos = eventFullDtos.stream()
                .skip(queryRequest.getFrom())
                .limit(queryRequest.getSize())
                .collect(Collectors.toList());
        List<EventShortDto> eventShortDtos = eventMapper.fullToShortDto(eventFullDtos);
        log.info("{} events was found in DB: {}", eventShortDtos.size(), eventShortDtos);
        sendStatistic(request);
        return eventShortDtos;
    }

    private BooleanExpression makeDateTimeCondition(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new ValidationException("End date cannot be earlier than start date");
            }
            return QEvent.event.eventDate.goe(rangeStart).and(QEvent.event.eventDate.loe(rangeEnd));
        } else if (rangeStart != null) {
            return QEvent.event.eventDate.goe(rangeStart);
        } else if (rangeEnd != null) {
            return QEvent.event.eventDate.loe(rangeEnd);
        } else {
            return QEvent.event.eventDate.isNotNull();
        }
    }

    private void sendStatistic(HttpServletRequest request) {
        HitDto hitDto = HitDto.builder()
                .app("ewm-main")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
        log.info("Client will send next info to statistic server: {}", hitDto);
        client.post(hitDto);
    }

    private Comparator<EventFullDto> getComparator(GetEventsByQueryRequest.Sort sort) {
        if (VIEWS.equals(sort)) {
            return Comparator.comparing(EventFullDto::getViews);
        }
        return Comparator.comparing(EventFullDto::getEventDate);
    }

    private void checkFieldsBeforeUpdate(Event event, UpdateEventRequest updatedEvent) {
        if (updatedEvent.getAnnotation() != null) {
            event.setAnnotation(updatedEvent.getAnnotation());
        }
        if (updatedEvent.getCategory() != null) {
            Category eventCat = categoryRepository.findById(updatedEvent.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with such id wasn't found"));
            event.setCategory(eventCat);
        }
        if (updatedEvent.getDescription() != null) {
            event.setDescription(updatedEvent.getDescription());
        }
        if (updatedEvent.getLocation() != null) {
            event.setLocation(updatedEvent.getLocation());
        }
        if (updatedEvent.getPaid() != null) {
            event.setPaid(updatedEvent.getPaid());
        }
        if (updatedEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updatedEvent.getParticipantLimit());
        }
        if (updatedEvent.getRequestModeration() != null) {
            event.setRequestModeration(updatedEvent.getRequestModeration());
        }
        if (updatedEvent.getTitle() != null) {
            event.setTitle(updatedEvent.getTitle());
        }
    }
}
