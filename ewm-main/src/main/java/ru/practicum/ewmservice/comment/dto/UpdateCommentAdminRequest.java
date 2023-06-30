package ru.practicum.ewmservice.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.ewmservice.comment.CommentStatus;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCommentAdminRequest {
    @NotNull
    private List<Long> commentIds;

    @NotNull
    private CommentStatus status;
}
