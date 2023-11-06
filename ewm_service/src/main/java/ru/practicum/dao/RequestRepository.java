package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.Status;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findByIdAndEventId(long requestId, long eventId);

    Optional<Request> findByIdAndRequesterId(long requestId, long requesterId);

    Optional<Request> findByRequesterIdAndEventId(long requesterId, long eventId);

    List<Request> findAllByRequesterId(long requesterId);

    List<Request> findAllByEventId(long eventId);

    List<Request> findAllByEventIdAndStatus(long eventId, Status status);
}
