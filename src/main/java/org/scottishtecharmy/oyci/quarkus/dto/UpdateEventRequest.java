package org.scottishtecharmy.oyci.quarkus.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 200, message = "Event name must be between 3 and 200 characters")
    private String eventName;

    @NotBlank(message = "Event type is required")
    @Size(min = 2, max = 150, message = "Event type must be between 2 and 150 characters")
    private String eventType;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "Event date is required")
    @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "Event date must be in dd/MM/yyyy format")
    private String eventDate;

    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Start time must be in HH:mm format (24-hour)")
    private String startTime;

    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "End time must be in HH:mm format (24-hour)")
    private String endTime;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 150, message = "Location must be between 2 and 150 characters")
    private String location;

    @NotNull(message = "Maximum attendees is required")
    @Min(value = 1, message = "Maximum attendees must be at least 1")
    @Max(value = 50, message = "Maximum attendees cannot exceed 50")
    private Integer maxAttendees;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status cannot exceed 50 characters")
    private String status;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Shift start must be in HH:mm format (24-hour)")
    private String shiftStart;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Shift end must be in HH:mm format (24-hour)")
    private String shiftEnd;

    @Valid
    private List<StaffAssignment> staffAssignments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffAssignment {

        @NotNull(message = "Staff ID is required")
        private Long staffId;

        @NotBlank(message = "Role is required")
        @Size(max = 100, message = "Role cannot exceed 100 characters")
        private String role;

        @NotBlank(message = "Shift start is required")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Shift start must be in HH:mm format (24-hour)")
        private String shiftStart;

        @NotBlank(message = "Shift end is required")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Shift end must be in HH:mm format (24-hour)")
        private String shiftEnd;
    }
}

