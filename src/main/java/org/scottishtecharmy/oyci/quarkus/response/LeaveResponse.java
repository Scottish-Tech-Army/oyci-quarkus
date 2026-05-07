package org.scottishtecharmy.oyci.quarkus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.model.Leave;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {

    private Long id;
    private Long staffId;
    private String staffName;
    private OffsetDateTime startDatetime;
    private OffsetDateTime endDatetime;
    private String leaveType;
    private String status;
    private String reason;

    public static LeaveResponse from(Leave leave) {
        LeaveResponse response = new LeaveResponse();
        response.setId(leave.getId());
        response.setStartDatetime(leave.getStartDatetime());
        response.setEndDatetime(leave.getEndDatetime());
        response.setLeaveType(leave.getLeaveType());
        response.setStatus(leave.getStatus());
        response.setReason(leave.getReason());

        if (leave.getStaff() != null) {
            response.setStaffId(leave.getStaff().getStaffId());
            if (leave.getStaff().getUser() != null) {
                response.setStaffName(
                    leave.getStaff().getUser().getFirstName() + " " +
                    leave.getStaff().getUser().getLastName()
                );
            }
        }
        return response;
    }
}

