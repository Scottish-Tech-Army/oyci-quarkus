package org.scottishtecharmy.oyci.quarkus.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.enums.LeaveType;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ApplyLeaveRequest {

    @NotNull(message = "Staff ID is required")
    private Long staffId;

    @NotNull(message = "Start date and time is required")
    private OffsetDateTime startDatetime;

    @NotNull(message = "End date and time is required")
    private OffsetDateTime endDatetime;

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    private String reason;
}
