package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dao.CommentReplyRepository;
import ru.practicum.comment.dao.CommentRepository;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentReply;
import ru.practicum.comment.model.dto.CommentDto;
import ru.practicum.comment.model.dto.CommentReplyDto;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.exception.model.BadRequestException;
import ru.practicum.exception.model.NotFoundException;
import ru.practicum.mapper.Mapper;
import ru.practicum.users.dao.UserRepository;
import ru.practicum.users.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService{
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final CommentReplyRepository commentReplyRepository;

    @Override
    @Transactional
    public CommentDto privateCreateComment(long userId, long eventId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " does not present in repository."));
        Event event = eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("User with id " + eventId + " does not present in repository."));
        if (event.getState().equals(State.PENDING)) {
            //возможность оставлять комментарии у публикатора события до его подтверждения/отклонения нахожу адекватным
            //к примеру, в формате "дополнительная информация" или "ответы на вероятные вопросы"
            if (event.getInitiator().getId() != user.getId()) {
                throw new BadRequestException("Only owner can set new comment for event in PENDING state.");
            }
        }
        Comment comment = createComment(user, event, commentDto);
        log.info("Sending to repository request to save new comment from user {} to event {}.", userId, eventId);
        return convertCommentToDtoWithReplies(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentReplyDto privateCreateCommentReply(long userId, long commentId, CommentReplyDto commentReplyDtoDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " does not present in repository."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id " + commentId + " does not present in repository."));
        CommentReply commentReply = Mapper.createCommentReply(user, comment, commentReplyDtoDto);
        log.info("Sending to repository request to save new comment reply from user {} to comment {}.", userId, commentId);
        return Mapper.convertCommentReplyToDto(commentReplyRepository.save(commentReply));
    }

    @Override
    @Transactional
    public CommentDto privateEditComment(long userId, long commentId, CommentDto commentDto) {
        Comment comment = commentRepository.findByIdAndCommentatorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment with id: " + commentId + " from user with id: " + userId + " does not present in repository."));
        comment.setText(commentDto.getText());
        comment.setEdited(true);
        comment.setEditedTime(LocalDateTime.now());
        log.info("Sending to repository request to save edited comment with id: {}.", commentId);
        return convertCommentToDtoWithReplies(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentReplyDto privateEditCommentReply(long userId, long commentReplyId, CommentReplyDto commentReplyDto) {
        CommentReply commentReply = commentReplyRepository.findByIdAndCommentatorId(commentReplyId, userId)
                .orElseThrow(() -> new NotFoundException("CommentReply with id: " + commentReplyId + " from user with id: " + userId + " does not present in repository."));
        commentReply.setText(commentReplyDto.getText());
        commentReply.setEdited(true);
        commentReply.setEditedTime(LocalDateTime.now());
        log.info("Sending to repository request to save edited comment reply with id: {}.", commentReplyId);
        return Mapper.convertCommentReplyToDto(commentReplyRepository.save(commentReply));
    }

    @Override
    @Transactional
    public List<CommentDto> publicGetEventComments(long eventId) {
        log.info("Sending to repository request to get event with id: {} comments.", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("User with id " + eventId + " does not present in repository."));
        if (event.getState().equals(State.PENDING)) {
            throw new BadRequestException("View comments for event in PENDING state unavailable.");
        }

        return commentRepository.findAllByEventId(eventId).stream()
                .map(this::convertCommentToDtoWithReplies)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto publicGetCommentById(long commentId) {
        log.info("Sending to repository request to get comment with id: {}.", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id: " + commentId + " does not present in repository."));
        if (comment.getEvent().getState().equals(State.PENDING)) {
            throw new BadRequestException("View comments for event in PENDING state unavailable.");
        }

        return convertCommentToDtoWithReplies(comment);
    }

    @Override
    @Transactional
    public void adminDeleteComment(long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id " + commentId + " does not present in repository."));
        log.info("Sending to repository request to delete comment with id: {} (from administrator).", comment.getId());
        commentRepository.deleteById(comment.getId());
    }

    @Override
    @Transactional
    public void adminDeleteCommentReply(long commentReplyId) {
        CommentReply commentReply = commentReplyRepository.findById(commentReplyId)
                .orElseThrow(() -> new NotFoundException("CommentReply with id: " + commentReplyId + " does not present in repository."));
        log.info("Sending to repository request to delete comment with id: {} (from administrator).", commentReply.getId());
        commentReplyRepository.deleteById(commentReply.getId());
    }

    @Override
    @Transactional
    public void privateDeleteComment(long userId, long commentId) {
        Comment comment = commentRepository.findByIdAndCommentatorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment with id: " + commentId + " from user with id: " + userId + " does not present in repository."));
        log.info("Sending to repository request to delete comment with id: {} (from commentator).", comment.getId());
        commentRepository.deleteById(comment.getId());
    }

    @Override
    @Transactional
    public void privateDeleteCommentReply(long userId, long commentReplyId) {
        CommentReply commentReply = commentReplyRepository.findByIdAndCommentatorId(commentReplyId, userId)
                .orElseThrow(() -> new NotFoundException("CommentReply with id: " + commentReplyId + " from user with id: " + userId + " does not present in repository."));
        log.info("Sending to repository request to delete comment with id: {} (from commentator).", commentReply.getId());
        commentReplyRepository.deleteById(commentReply.getId());
    }

    private CommentDto convertCommentToDtoWithReplies(Comment comment) {
        return Mapper.convertCommentToDto(comment, commentReplyRepository.findAllByCommentId(comment.getId()));
    }

    private Comment createComment(User user, Event event, CommentDto commentDto) {
        return Mapper.createComment(user, event, commentDto);
    }
}
