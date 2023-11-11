package ru.practicum.comment.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comment.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    public Optional<Comment> findByIdAndCommentatorId(long commentId, long commentatorId);
    public List<Comment> findAllByEventId(long eventId);
}
