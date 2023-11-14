package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.model.dto.CommentDto;
import ru.practicum.comment.model.dto.CommentReplyDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CommentDto privateCreateComment(@PathVariable long userId, @PathVariable long eventId,
                                           @RequestBody @Valid CommentDto commentDto) {
        log.info("Received request to add new comment for event {}  from user: {}.", eventId, userId);
        return commentService.privateCreateComment(userId, eventId, commentDto);
    }

    @PostMapping("/users/{userId}/comments/{commentId}/reply")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CommentReplyDto privateCreateCommentReply(@PathVariable long userId, @PathVariable long commentId,
                                                     @RequestBody @Valid CommentReplyDto commentReplyDto) {
        log.info("Received request to add new reply for comment {}  from user: {}.", commentId, userId);
        return commentService.privateCreateCommentReply(userId, commentId, commentReplyDto);
    }

    @PatchMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CommentDto privateEditComment(@PathVariable long userId, @PathVariable long commentId,
                                         @RequestBody @Valid CommentDto commentDto) {
        log.info("Received request to edit comment with id: {}.", commentId);
        return commentService.privateEditComment(userId, commentId, commentDto);
    }

    @PatchMapping("/users/{userId}/comments/reply/{commentReplyId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CommentReplyDto privateEditCommentReply(@PathVariable long userId, @PathVariable long commentReplyId,
                                                   @RequestBody @Valid CommentReplyDto commentReplyDto) {
        log.info("Received request to edit comment reply with id: {}.", commentReplyId);
        return commentService.privateEditCommentReply(userId, commentReplyId, commentReplyDto);
    }

    @GetMapping("/events/{eventId}/comments")
    @ResponseStatus(code = HttpStatus.OK)
    public List<CommentDto> publicGetEventComments(@PathVariable long eventId) {
        log.info("Received request to get event {} comments.", eventId);
        return commentService.publicGetEventComments(eventId);
    }

    @GetMapping("/comments/{commentId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CommentDto publicGetCommentById(@PathVariable long commentId) {
        log.info("Received request to get comment with id: {}.", commentId);
        return commentService.publicGetCommentById(commentId);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void adminDeleteComment(@PathVariable long commentId) {
        log.info("Received request to delete comment with id: {} (from administrator).", commentId);
        commentService.adminDeleteComment(commentId);
    }

    @DeleteMapping("/admin/comments/reply/{commentReplyId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void adminDeleteCommentReply(@PathVariable long commentReplyId) {
        log.info("Received request to delete comment reply with id: {} (from administrator).", commentReplyId);
        commentService.adminDeleteCommentReply(commentReplyId);
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void privateDeleteComment(@PathVariable long userId, @PathVariable long commentId) {
        log.info("Received request to delete comment with id: {} (from commentator).", commentId);
        commentService.privateDeleteComment(userId, commentId);
    }

    @DeleteMapping("/users/{userId}/comments/reply/{commentReplyId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void privateDeleteCommentReply(@PathVariable long userId, @PathVariable long commentReplyId) {
        log.info("Received request to delete comment reply with id: {} (from commentator).", commentReplyId);
        commentService.privateDeleteCommentReply(userId, commentReplyId);
    }
}
