package org.scottishtecharmy.oyci.quarkus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStaffRequest {

    // User details
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Middle name cannot exceed 100 characters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    private LocalDate dob;

    @NotNull(message = "Role ID is required")
    @Positive(message = "Role ID must be a positive number")
    private Long roleId;


    // Contact details
    @NotBlank(message = "Primary email is required")
    @Email(message = "Primary email must be valid")
    @Size(max = 255, message = "Primary email cannot exceed 255 characters")
    private String primaryEmail;

    @Email(message = "Secondary email must be valid")
    @Size(max = 255, message = "Secondary email cannot exceed 255 characters")
    private String secondaryEmail;

    @Size(max = 30, message = "Primary phone cannot exceed 30 characters")
    private String primaryPhone;

    @Size(max = 30, message = "Secondary phone cannot exceed 30 characters")
    private String secondaryPhone;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 cannot exceed 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 cannot exceed 255 characters")
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @NotBlank(message = "Postcode is required")
    @Size(max = 20, message = "Postcode cannot exceed 20 characters")
    private String postcode;

    // Staff details
    @NotNull(message = "Staff type ID is required")
    @Positive(message = "Staff type ID must be a positive number")
    private Long staffTypeId;

    @Positive(message = "Qualification ID must be a positive number")
    private Long qualificationId;

    @Positive(message = "Weekly hours cap must be a positive number")
    private Integer weeklyHoursCap;

    private List<String> preferredShiftTimes;

    private Boolean isActive = true;

}
