package ru.practicum.comment.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comment.model.CommentReply;

import java.util.List;
import java.util.Optional;

public interface CommentReplyRepository extends JpaRepository<CommentReply, Long> {
    Optional<CommentReply> findByIdAndCommentatorId(long commentReplyId, long commentatorId);

    List<CommentReply> findAllByCommentId(long commentId);

    List<CommentReply> findAllByCommentIdIn(List<Long> commentsIds);
}
