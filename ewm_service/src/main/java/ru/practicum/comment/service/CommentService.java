package ru.practicum.comment.service;

import ru.practicum.comment.model.dto.CommentDto;
import ru.practicum.comment.model.dto.CommentReplyDto;

import java.util.List;

public interface CommentService {
    CommentDto privateCreateComment(long userId, long eventId, CommentDto commentDto);

    CommentReplyDto privateCreateCommentReply(long userId, long commentId, CommentReplyDto commentReplyDtoDto);

    CommentDto privateEditComment(long userId, long commentId, CommentDto commentDto);

    CommentReplyDto privateEditCommentReply(long userId, long commentReplyId, CommentReplyDto commentReplyDtoDto);

    List<CommentDto> publicGetEventComments(long eventId);

    CommentDto publicGetCommentById(long commentId);

    void adminDeleteComment(long commentId);

    void adminDeleteCommentReply(long commentReplyId);

    void privateDeleteComment(long userId, long commentId);

    void privateDeleteCommentReply(long userId, long commentReplyId);
}
