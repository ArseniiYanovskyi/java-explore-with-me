package ru.practicum.comment.service;

import ru.practicum.comment.model.dto.CommentDto;
import ru.practicum.comment.model.dto.CommentReplyDto;

import java.util.List;

public interface CommentService {
    public CommentDto privateCreateComment(long userId, long eventId, CommentDto commentDto);
    public CommentReplyDto privateCreateCommentReply(long userId, long commentId, CommentReplyDto commentReplyDtoDto);
    public CommentDto privateEditComment(long userId, long commentId, CommentDto commentDto);
    public CommentReplyDto privateEditCommentReply(long userId, long commentReplyId, CommentReplyDto commentReplyDtoDto);
    public List<CommentDto> publicGetEventComments(long eventId);
    public CommentDto publicGetCommentById(long commentId);
    public void adminDeleteComment(long commentId);
    public void adminDeleteCommentReply(long commentReplyId);
    public void privateDeleteComment(long userId, long commentId);
    public void privateDeleteCommentReply(long userId, long commentReplyId);
}
