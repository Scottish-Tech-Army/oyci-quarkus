package org.scottishtecharmy.oyci.quarkus.model;

import lombok.Getter;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.entity.AuditableEntity;
import jakarta.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "staff", uniqueConstraints = {
    @UniqueConstraint(name = "uq_staff_user", columnNames = {"user_id"})
})
public class Staff extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_type_id", nullable = false)
    private StaffType staffType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qualification_id")
    private Qualification qualification;

    @Column(name = "weekly_hours_cap")
    private Integer weeklyHoursCap;

    @Column(name = "preferred_shift_times", length = 1000)
    @Convert(converter = StringListConverter.class)
    private List<String> preferredShiftTimes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}
