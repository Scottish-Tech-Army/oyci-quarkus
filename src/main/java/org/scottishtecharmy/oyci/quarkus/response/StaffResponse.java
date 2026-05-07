package org.scottishtecharmy.oyci.quarkus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.model.Staff;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {

    private Long staffId;
    private Long userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phone;
    private Long staffTypeId;
    private String staffTypeName;
    private String qualificationName;
    private Long roleId;
    private Boolean isActive;
    private List<AvailabilityResponse> availability;

    public static StaffResponse from(Staff staff) {
        Long userId = staff.getUser() != null ? staff.getUser().getUserId() : null;
        String firstName = staff.getUser() != null ? staff.getUser().getFirstName() : null;
        String middleName = staff.getUser() != null ? staff.getUser().getMiddleName() : null;
        String lastName = staff.getUser() != null ? staff.getUser().getLastName() : null;

        String email = null;
        String phone = null;
        if (staff.getUser() != null && staff.getUser().getContactDetail() != null) {
            email = staff.getUser().getContactDetail().getPrimaryEmail();
            phone = staff.getUser().getContactDetail().getPrimaryPhone();
        }

        // Get roleId from user.roleId
        Long roleId = null;
        if (staff.getUser() != null && staff.getUser().getRoleId() != null) {
            roleId = staff.getUser().getRoleId().getRoleId();
        }

        Long staffTypeId = staff.getStaffType() != null ? staff.getStaffType().getStaffTypeId() : null;
        String staffTypeName = staff.getStaffType() != null ? staff.getStaffType().getName() : null;
        String qualificationName = staff.getQualification() != null ? staff.getQualification().getName() : null;

        // Convert preferred shift times to availability slots
        List<AvailabilityResponse> availability = new ArrayList<>();
        if (staff.getPreferredShiftTimes() != null && !staff.getPreferredShiftTimes().isEmpty()) {
            availability = staff.getPreferredShiftTimes().stream()
                    .map(AvailabilityResponse::fromShiftTime)
                    .filter(av -> av != null) // Remove nulls from failed parsing
                    .collect(Collectors.toList());
        }

        return new StaffResponse(
                staff.getStaffId(),
                userId,
                firstName,
                middleName,
                lastName,
                email,
                phone,
                staffTypeId,
                staffTypeName,
                qualificationName,
                roleId,
                staff.getIsActive(),
                availability
        );
    }
}
