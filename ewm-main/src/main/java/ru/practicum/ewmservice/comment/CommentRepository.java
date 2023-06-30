package ru.practicum.ewmservice.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, QuerydslPredicateExecutor<Comment> {

    Page<Comment> findByCommentatorId(Long commentatorId, Pageable pageable);

    List<Comment> findByCommentatorIdAndEventId(Long userId, Long eventId);

    List<Comment> findByIdIn(List<Long> commentIds);

    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status);
}
