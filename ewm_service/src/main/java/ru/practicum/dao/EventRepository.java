package ru.practicum.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    public List<Event> findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateIsBetween(List<Long> users, List<State> states, List<Long> categories, Timestamp start, Timestamp end);
    public List<Event> findAllByStateIsAndCategoryIdInAndEventDateIsBetweenOrderByEventDateAsc(State state, List<Integer> categories, Timestamp start, Timestamp end);
    public List<Event> findAllByStateIsAndCategoryIdInAndEventDateIsBetweenOrderByViewsDesc(State state, List<Integer> categories, Timestamp start, Timestamp end);
    public List<Event> findAllByStateIsAndEventDateIsAfterAndCategoryIdInOrderByEventDateAsc(State state, Timestamp now, List<Integer> categories);
    public List<Event> findAllByStateIsAndEventDateIsAfterAndCategoryIdInOrderByViewsDesc(State state, Timestamp now, List<Integer> categories);
    public Optional<Event> findByIdIsAndStateIs(long eventId, State state);
    public List<Event> findAllByInitiatorId(long initiatorId, Pageable page);
}
