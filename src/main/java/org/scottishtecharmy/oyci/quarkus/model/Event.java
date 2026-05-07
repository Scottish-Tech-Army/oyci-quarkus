package org.scottishtecharmy.oyci.quarkus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.entity.AuditableEntity;
import org.scottishtecharmy.oyci.quarkus.enums.EventStatus;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "event", indexes = {
    @Index(name = "idx_event_time", columnList = "start_datetime, end_datetime")
})
public class Event extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_name", nullable = false, length = 200)
    private String eventName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", nullable = false)
    private EventType eventType;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "start_datetime", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime endDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private EventStatus status;

    @Column(name = "max_attendees")
    private Integer maxAttendees;

}
