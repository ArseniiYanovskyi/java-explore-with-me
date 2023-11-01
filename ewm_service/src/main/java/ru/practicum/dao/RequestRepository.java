package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.Status;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    public Optional<Request> findByRequesterIdAndEventId(long requesterId, long eventId);
    public List<Request> findAllByRequesterId(long requesterId);
    public List<Request> findAllByEventId(long eventId);
    public List<Request> findAllByEventIdAAndStatus(long eventId, Status status);
}
