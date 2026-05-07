package org.scottishtecharmy.oyci.quarkus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.entity.AuditableEntity;

@Getter
@Setter
@Entity
@Table(name = "contact_detail")
public class ContactDetail extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_detail_id")
    private Long contactDetailId;

    @Column(name = "primary_email", nullable = false, length = 255)
    private String primaryEmail;

    @Column(name = "secondary_email", length = 255)
    private String secondaryEmail;

    @Column(name = "primary_phone", length = 30)
    private String primaryPhone;

    @Column(name = "secondary_phone", length = 30)
    private String secondaryPhone;

    // Address fields moved from Address table
    @Column(name = "address_line_1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line_2", length = 255)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "postcode", nullable = false, length = 20)
    private String postcode;

}
