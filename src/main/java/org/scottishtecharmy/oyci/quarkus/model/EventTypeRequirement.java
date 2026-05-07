package org.scottishtecharmy.oyci.quarkus.model;

import lombok.Getter;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.entity.AuditableEntity;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "event_type_requirement")
@IdClass(EventTypeRequirementId.class)
public class EventTypeRequirement extends AuditableEntity {

    @Id
    @Column(name = "event_type_id", nullable = false)
    private Long eventTypeId;

    @Id
    @Column(name = "qualification_id", nullable = false)
    private Long qualificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", insertable = false, updatable = false)
    private EventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qualification_id", insertable = false, updatable = false)
    private Qualification qualification;

    @Column(name = "requirement_level", nullable = false, length = 20)
    private String requirementLevel;

}
