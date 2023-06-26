package ru.practicum.ewmservice.request;

import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.request.dto.ParticipationRequestDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequestMapper {

    public ParticipationRequestDto mapToDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .status(request.getStatus())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .build();
    }

    public List<ParticipationRequestDto> mapToDto(List<Request> requests) {
        if (requests == null) {
            return Collections.emptyList();
        }
        return requests.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}
