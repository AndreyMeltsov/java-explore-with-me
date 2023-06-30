package ru.practicum.ewmservice.comment;

import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.comment.dto.CommentDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public CommentDto mapToDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .commentatorId(comment.getCommentator().getId())
                .eventId(comment.getEvent().getId())
                .status(comment.getStatus())
                .modified(comment.getModified())
                .build();
    }

    public List<CommentDto> mapToDto(List<Comment> comments) {
        if (comments == null) {
            return Collections.emptyList();
        }
        return comments.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}
