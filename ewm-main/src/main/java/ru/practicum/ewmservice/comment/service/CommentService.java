package ru.practicum.ewmservice.comment.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewmservice.comment.dto.CommentDto;
import ru.practicum.ewmservice.comment.dto.NewCommentDto;
import ru.practicum.ewmservice.comment.dto.UpdateCommentAdminRequest;

import java.util.List;

public interface CommentService {

    CommentDto create(Long userId, Long eventId, NewCommentDto commentDto);

    List<CommentDto> getAll(Long userId, Pageable pageable);

    List<CommentDto> getByEventId(Long userId, Long eventId);

    CommentDto updateByUser(Long userId, Long commentId, NewCommentDto commentDto);

    void delete(Long userId, Long commentId);

    List<CommentDto> getByParams(Long userId, Long eventId, String status, Pageable pageable);

    List<CommentDto> updateByAdmin(UpdateCommentAdminRequest adminRequest);
}
