package org.scottishtecharmy.oyci.quarkus.model;

import lombok.Getter;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.entity.AuditableEntity;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "role", uniqueConstraints = {
    @UniqueConstraint(name = "uq_role_name", columnNames = {"name"})
})
public class Role extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "role_type", nullable = false, length = 100)
    private String roleType;

}