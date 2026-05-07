package org.scottishtecharmy.oyci.quarkus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.model.Staff;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffDetailResponse {

    // Basic staff info
    private Long staffId;
    private Long userId;

    // User details
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dob;
    private Long roleId;

    // Contact details
    private String primaryEmail;
    private String secondaryEmail;
    private String primaryPhone;
    private String secondaryPhone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String country;
    private String postcode;

    // Staff type and qualification
    private Long staffTypeId;
    private String staffTypeName;
    private Long qualificationId;
    private String qualificationName;

    // Staff specific details
    private Integer weeklyHoursCap;
    private List<String> preferredShiftTimes;
    private Boolean isActive;

    public static StaffDetailResponse from(Staff staff) {
        // User details
        Long userId = null;
        String firstName = null;
        String middleName = null;
        String lastName = null;
        LocalDate dob = null;
        Long roleId = null;

        if (staff.getUser() != null) {
            userId = staff.getUser().getUserId();
            firstName = staff.getUser().getFirstName();
            middleName = staff.getUser().getMiddleName();
            lastName = staff.getUser().getLastName();
            dob = staff.getUser().getDob();

            if (staff.getUser().getRoleId() != null) {
                roleId = staff.getUser().getRoleId().getRoleId();
            }
        }

        // Contact details
        String primaryEmail = null;
        String secondaryEmail = null;
        String primaryPhone = null;
        String secondaryPhone = null;
        String addressLine1 = null;
        String addressLine2 = null;
        String city = null;
        String country = null;
        String postcode = null;

        if (staff.getUser() != null && staff.getUser().getContactDetail() != null) {
            primaryEmail = staff.getUser().getContactDetail().getPrimaryEmail();
            secondaryEmail = staff.getUser().getContactDetail().getSecondaryEmail();
            primaryPhone = staff.getUser().getContactDetail().getPrimaryPhone();
            secondaryPhone = staff.getUser().getContactDetail().getSecondaryPhone();
            addressLine1 = staff.getUser().getContactDetail().getAddressLine1();
            addressLine2 = staff.getUser().getContactDetail().getAddressLine2();
            city = staff.getUser().getContactDetail().getCity();
            country = staff.getUser().getContactDetail().getCountry();
            postcode = staff.getUser().getContactDetail().getPostcode();
        }

        // Staff type
        Long staffTypeId = null;
        String staffTypeName = null;
        if (staff.getStaffType() != null) {
            staffTypeId = staff.getStaffType().getStaffTypeId();
            staffTypeName = staff.getStaffType().getName();
        }

        // Qualification
        Long qualificationId = null;
        String qualificationName = null;
        if (staff.getQualification() != null) {
            qualificationId = staff.getQualification().getQualificationId();
            qualificationName = staff.getQualification().getName();
        }

        return new StaffDetailResponse(
                staff.getStaffId(),
                userId,
                firstName,
                middleName,
                lastName,
                dob,
                roleId,
                primaryEmail,
                secondaryEmail,
                primaryPhone,
                secondaryPhone,
                addressLine1,
                addressLine2,
                city,
                country,
                postcode,
                staffTypeId,
                staffTypeName,
                qualificationId,
                qualificationName,
                staff.getWeeklyHoursCap(),
                staff.getPreferredShiftTimes(),
                staff.getIsActive()
        );
    }
}
