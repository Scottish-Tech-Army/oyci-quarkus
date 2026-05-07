package org.scottishtecharmy.oyci.quarkus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.entity.AuditableEntity;

@Getter
@Setter
@Entity
@Table(name = "event_type", uniqueConstraints = {
    @UniqueConstraint(name = "uq_event_type_name", columnNames = {"name"})
})
public class EventType extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_type_id")
    private Long eventTypeId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

}
