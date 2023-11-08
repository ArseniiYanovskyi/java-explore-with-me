package ru.practicum.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.category.model.Category;
import ru.practicum.users.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;
    @NotBlank
    @Column(name = "description", nullable = false)
    private String description;
    @NotBlank
    @Column(name = "annotation", nullable = false)
    private String annotation;
    @JoinColumn(name = "initiator_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User initiator;
    @JoinColumn(name = "category_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Category category;
    @Column(name = "longitude", nullable = false)
    private double longitude;
    @Column(name = "latitude", nullable = false)
    private double latitude;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @Column(name = "participant_limit")
    private int participantLimit;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(name = "published_time")
    private LocalDateTime publishedTime;
    @Column(name = "paid", nullable = false)
    private boolean paid;
    @Column(name = "request_moderation")
    private boolean requestModeration;
    @Column(name = "confirmed_requests", nullable = false)
    private int confirmedRequests;
    @Column(name = "state_condition", nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;
}
