package ru.practicum.ewmservice.comment.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.comment.dto.CommentDto;
import ru.practicum.ewmservice.comment.dto.UpdateCommentAdminRequest;
import ru.practicum.ewmservice.comment.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/comments")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getComments(@RequestParam(value = "user", required = false) Long userId,
                                        @RequestParam(value = "event", required = false) Long eventId,
                                        @RequestParam(value = "status", required = false) String status,
                                        @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") Integer from,
                                        @Positive @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Pageable pageRequest = PageRequest.of(from / size, size);
        return commentService.getByParams(userId, eventId, status, pageRequest);
    }

    @PatchMapping
    public List<CommentDto> update(@Valid @RequestBody UpdateCommentAdminRequest adminRequest) {
        return commentService.updateByAdmin(adminRequest);
    }
}
