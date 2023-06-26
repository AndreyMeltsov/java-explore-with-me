package ru.practicum.ewmservice.request.service;

import ru.practicum.ewmservice.request.dto.EventRequestStatusUpdateDto;
import ru.practicum.ewmservice.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewmservice.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getRequestsByUserIdAndEventId(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId, EventRequestStatusUpdateDto updatedRequest);

    List<ParticipationRequestDto> findByUserId(Long userId);

    ParticipationRequestDto create(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}
