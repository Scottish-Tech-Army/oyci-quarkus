package org.scottishtecharmy.oyci.quarkus.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.scottishtecharmy.oyci.quarkus.enums.EventStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "event_instances")
public class EventInstance extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_type_id", nullable = false)
    public EventType eventType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    public Location location;

    @Column(name = "event_date", nullable = false)
    public LocalDate eventDate;

    @Column(name = "start_time", nullable = false)
    public LocalTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public EventStatus status = EventStatus.DRAFT;

    @Column(name = "capacity_override")
    public Integer capacityOverride;

    public static List<EventInstance> findByFilters(LocalDate from, LocalDate to, EventStatus status) {
        if (from != null && to != null && status != null) {
            return list("eventDate >= ?1 AND eventDate <= ?2 AND status = ?3", from, to, status);
        } else if (from != null && to != null) {
            return list("eventDate >= ?1 AND eventDate <= ?2", from, to);
        } else if (status != null) {
            return list("status", status);
        }
        return listAll();
    }
}

