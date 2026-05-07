package org.scottishtecharmy.oyci.quarkus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.entity.AuditableEntity;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "registration")
public class Registration extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Long registrationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrant_user_id", nullable = false)
    private User registrant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_user_id", nullable = false)
    private User registeredBy;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, WAITLISTED, CANCELLED

}
