package ru.practicum.serviceutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatisticClient;
import ru.practicum.comment.dao.CommentReplyRepository;
import ru.practicum.comment.dao.CommentRepository;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentReply;
import ru.practicum.dto.StatisticAnswerDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.dto.EventFullDto;
import ru.practicum.event.model.dto.EventShortDto;
import ru.practicum.mapper.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ServiceUtils {
    private final CommentRepository commentRepository;
    private final CommentReplyRepository commentReplyRepository;
    private final StatisticClient statisticClient;
    private final ObjectMapper objectMapper;

    public EventFullDto convertEventToFullDto(Event event) {
        List<Comment> comments = commentRepository.findAllByEventId(event.getId());
        List<CommentReply> replies = commentReplyRepository.findAllByCommentIdInOrderByIdAsc(comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList()));
        return Mapper.convertEventToFullDto(event, comments, replies, getUniqueViews(event.getId()));
    }

    public EventShortDto convertEventToShortDto(Event event) {
        return Mapper.convertEventToShortDto(event, getUniqueViews(event.getId()));
    }

    private long getUniqueViews(long eventId) {
        ResponseEntity<Object> statisticAnswer = statisticClient.getUniqueStatisticByEventId(eventId);
        try {
            StatisticAnswerDto result = objectMapper.convertValue(statisticAnswer.getBody(), StatisticAnswerDto.class);
            return result.getHits();
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }
}