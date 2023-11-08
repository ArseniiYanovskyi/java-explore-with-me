package ru.practicum.serviceutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatisticClient;
import ru.practicum.dto.StatisticAnswerDto;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.dto.EventFullDto;
import ru.practicum.event.model.dto.EventShortDto;
import ru.practicum.exception.model.NotFoundException;
import ru.practicum.mapper.Mapper;

@Component
@AllArgsConstructor
public class ServiceUtils {
    private final EventRepository eventRepository;
    private final StatisticClient statisticClient;
    private final ObjectMapper objectMapper;

    public Event getEventWithOwnershipCheck(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " does not present in repository."));
        if (event.getInitiator().getId() != userId) {
            throw new NotFoundException("Event with id " + eventId + " does now belong to user id " + userId + ".");
        }
        return event;
    }

    public EventFullDto convertEventToFullDto(Event event) {
        return Mapper.convertEventToFullDto(event, getUniqueViews(event.getId()));
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