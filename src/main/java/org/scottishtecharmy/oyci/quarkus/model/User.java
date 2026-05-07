package org.scottishtecharmy.oyci.quarkus.model;

import lombok.Getter;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.entity.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role roleId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "dob")
    private LocalDate dob;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_detail_id")
    private ContactDetail contactDetail;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}