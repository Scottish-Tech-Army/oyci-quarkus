package org.scottishtecharmy.oyci.quarkus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.entity.AuditableEntity;

@Getter
@Setter
@Entity
@Table(name = "staff_type", uniqueConstraints = {
    @UniqueConstraint(name = "uq_staff_type_name", columnNames = {"name"})
})
public class StaffType extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_type_id")
    private Long staffTypeId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

}

