package ru.practicum.ewmservice.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.event.Event;
import ru.practicum.ewmservice.event.EventRepository;
import ru.practicum.ewmservice.event.EventState;
import ru.practicum.ewmservice.exception.ConflictException;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.request.Request;
import ru.practicum.ewmservice.request.RequestMapper;
import ru.practicum.ewmservice.request.RequestRepository;
import ru.practicum.ewmservice.request.RequestStatus;
import ru.practicum.ewmservice.request.dto.EventRequestStatusUpdateDto;
import ru.practicum.ewmservice.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewmservice.request.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.user.User;
import ru.practicum.ewmservice.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getRequestsByUserIdAndEventId(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with such id wasn't found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User cannot get requests for event in which he isn't initiator");
        }
        List<ParticipationRequestDto> requestDtos = requestMapper.mapToDto(requestRepository.findByEventId(eventId));
        log.info("{} requests was found in DB: {}", requestDtos.size(), requestDtos);
        return requestDtos;
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId,
                                                         EventRequestStatusUpdateDto updatedRequest) {
        EventRequestStatusUpdateResult result;
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with such id wasn't found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only initiator can change request's statuses");
        }

        List<Request> requests = requestRepository.findByIdIn(updatedRequest.getRequestIds());

        if ((!event.isRequestModeration() || event.getParticipantLimit() == 0)
                && updatedRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            result = new EventRequestStatusUpdateResult(requestMapper.mapToDto(requests), Collections.emptyList());
            log.info("Status confirmation isn't required: {}", result);
            return result;
        }

        int vacancyLeft = event.getParticipantLimit() - getConfirmedRequests(eventId);

        if (vacancyLeft <= 0) {
            throw new ConflictException("The participant limit has been already reached");
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();

        for (Request request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Request must have status PENDING");
            }
            if (updatedRequest.getStatus().equals(RequestStatus.CONFIRMED) && vacancyLeft > 0) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);
                vacancyLeft--;
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        }

        if (!confirmedRequests.isEmpty()) {
            requestRepository.saveAll(confirmedRequests);
        }
        if (!rejectedRequests.isEmpty()) {
            requestRepository.saveAll(rejectedRequests);
        }

        result = new EventRequestStatusUpdateResult(requestMapper.mapToDto(confirmedRequests),
                requestMapper.mapToDto(rejectedRequests));
        log.info("Status requests update result was received: {}", result);
        return result;
    }

    @Override
    public List<ParticipationRequestDto> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with such id wasn't found");
        }
        List<ParticipationRequestDto> requestDtos = requestMapper.mapToDto(requestRepository.findByRequesterId(userId));
        log.info("{} requests was found in DB: {}", requestDtos.size(), requestDtos);
        return requestDtos;
    }

    @Transactional
    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with such id wasn't found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with such id wasn't found"));

        if (Boolean.TRUE.equals(requestRepository.existsByEventIdAndRequesterId(eventId, userId))) {
            throw new ConflictException(
                    String.format("Request for eventId = %d from userId = %d already exists", eventId, userId));
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("The event initiator cannot add requests for this event");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("It is not possible to apply for an unpublished event");
        }
        int limit = event.getParticipantLimit();

        if (limit != 0 && limit <= getConfirmedRequests(eventId)) {
            throw new ConflictException("The participant limit has been reached");
        }
        Request request = Request.builder()
                .event(event)
                .requester(user)
                .status(!event.isRequestModeration() || event.getParticipantLimit() == 0
                        ? RequestStatus.CONFIRMED : RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
        ParticipationRequestDto requestDto = requestMapper.mapToDto(requestRepository.save(request));
        log.info("Request was added: {}", requestDto);
        return requestDto;
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Request with such id wasn't found"));
        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("Only the owner can update the request");
        }
        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequestDto requestDto = requestMapper.mapToDto(requestRepository.save(request));
        log.info("Request was canceled: {}", requestDto);
        return requestDto;
    }

    private Integer getConfirmedRequests(Long eventId) {
        return requestRepository.findCountRequestsByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }
}
