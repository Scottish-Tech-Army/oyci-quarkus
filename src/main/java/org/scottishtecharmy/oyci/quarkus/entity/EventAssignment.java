package org.scottishtecharmy.oyci.quarkus.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "event_assignments")
public class EventAssignment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_instance_id", nullable = false)
    public EventInstance eventInstance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    public static List<EventAssignment> findByEventInstanceId(Long eventInstanceId) {
        return list("eventInstance.id", eventInstanceId);
    }

    public static List<EventAssignment> findByUserId(Long userId) {
        return list("user.id", userId);
    }

    public static EventAssignment findByEventAndUser(Long eventInstanceId, Long userId) {
        return find("eventInstance.id = ?1 AND user.id = ?2", eventInstanceId, userId).firstResult();
    }
}

