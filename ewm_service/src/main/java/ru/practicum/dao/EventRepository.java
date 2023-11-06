package ru.practicum.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByCategoryId(long categoryId);

    List<Event> findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(List<Long> users, List<State> states, List<Long> categories, LocalDateTime start, LocalDateTime end);

    List<Event> findAllByStateIsAndCategoryIdInAndEventDateBetween(State state, List<Long> categories, LocalDateTime start, LocalDateTime end);

    List<Event> findAllByStateIsAndEventDateAfterAndCategoryIdIn(State state, LocalDateTime now, List<Long> categories);

    Optional<Event> findByIdIsAndStateIs(long eventId, State state);

    List<Event> findAllByInitiatorId(long initiatorId, Pageable page);
}
