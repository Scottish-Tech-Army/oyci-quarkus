package org.scottishtecharmy.oyci.quarkus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response wrapper for list-staff endpoint
 * Contains all staff information and count of active staff
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffListResponse {

    /**
     * List of all staff with their details
     */
    private List<StaffDetailResponse> staffInfo;

    /**
     * Count of staff with isActive = true
     */
    private long activeStaff;
}

