package ru.practicum.ewmservice.comment.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.comment.Comment;
import ru.practicum.ewmservice.comment.CommentMapper;
import ru.practicum.ewmservice.comment.CommentRepository;
import ru.practicum.ewmservice.comment.CommentStatus;
import ru.practicum.ewmservice.comment.QComment;
import ru.practicum.ewmservice.comment.dto.CommentDto;
import ru.practicum.ewmservice.comment.dto.NewCommentDto;
import ru.practicum.ewmservice.comment.dto.UpdateCommentAdminRequest;
import ru.practicum.ewmservice.event.Event;
import ru.practicum.ewmservice.event.EventRepository;
import ru.practicum.ewmservice.event.EventState;
import ru.practicum.ewmservice.exception.ConflictException;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.user.User;
import ru.practicum.ewmservice.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto create(Long userId, Long eventId, NewCommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with such id wasn't found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with such id wasn't found"));
        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("It is not possible to create a comment for an unpublished event");
        }
        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .event(event)
                .commentator(user)
                .created(LocalDateTime.now())
                .status(CommentStatus.PENDING)
                .build();
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment was added: {}", savedComment);
        return commentMapper.mapToDto(savedComment);
    }

    @Override
    public List<CommentDto> getAll(Long userId, Pageable pageable) {
        List<CommentDto> comments = commentMapper.mapToDto(commentRepository
                .findByCommentatorId(userId, pageable)
                .getContent());
        log.info("{} comments were found in DB: {}", comments.size(), comments);
        return comments;
    }

    @Override
    public List<CommentDto> getByEventId(Long userId, Long eventId) {
        List<CommentDto> comments = commentMapper.mapToDto(commentRepository
                .findByCommentatorIdAndEventId(userId, eventId));
        log.info("{} comments were found in DB: {}", comments.size(), comments);
        return comments;
    }

    @Override
    @Transactional
    public CommentDto updateByUser(Long userId, Long commentId, NewCommentDto commentDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with such id wasn't found"));

        if (!comment.getCommentator().getId().equals(userId)) {
            throw new ConflictException("Only the commentator or administrator can update a comment");
        }
        if (commentDto.getText() != null) {
            comment.setText(commentDto.getText());
        }
        comment.setModified(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);
        log.info("Comment was updated in DB by user. New comment is: {}", comment);
        return commentMapper.mapToDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with such id wasn't found"));
        if (!comment.getCommentator().getId().equals(userId)) {
            throw new ConflictException("Only commentator can delete a comment");
        }
        commentRepository.deleteById(commentId);
        log.info("Comment with id = {} was deleted", commentId);
    }

    @Override
    public List<CommentDto> getByParams(Long userId, Long eventId, String status, Pageable pageable) {
        QComment comment = QComment.comment;
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (userId != null) {
            booleanBuilder.and(comment.commentator.id.eq(userId));
        }
        if (eventId != null) {
            booleanBuilder.and(comment.event.id.eq(eventId));
        }
        if (status != null) {
            booleanBuilder.and(comment.status.eq(CommentStatus.valueOf(status.toUpperCase())));
        }
        List<Comment> comments = booleanBuilder.getValue() != null
                ? commentRepository.findAll(booleanBuilder.getValue(), pageable).getContent()
                : commentRepository.findAll(pageable).getContent();

        List<CommentDto> commentDtos = commentMapper.mapToDto(comments);
        log.info("{} comments were found in DB: {}", commentDtos.size(), commentDtos);
        return commentDtos;
    }

    @Override
    @Transactional
    public List<CommentDto> updateByAdmin(UpdateCommentAdminRequest adminRequest) {
        List<Comment> comments = commentRepository.findByIdIn(adminRequest.getCommentIds());
        comments.forEach(comment -> {
            comment.setStatus(adminRequest.getStatus());
            commentRepository.save(comment);
        });
        List<CommentDto> commentDtos = commentMapper.mapToDto(comments);
        log.info("Comments was updated in DB by admin. New comments are: {}", commentDtos);
        return commentDtos;
    }


}
