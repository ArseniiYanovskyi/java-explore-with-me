package ru.practicum.event.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByCategoryId(long categoryId);
    Optional<Event> findByIdAndInitiatorId(long eventId, long initiatorId);
    Optional<Event> findByIdIsAndStateIs(long eventId, State state);
    List<Event> findAllByInitiatorId(long initiatorId, Pageable page);
}
