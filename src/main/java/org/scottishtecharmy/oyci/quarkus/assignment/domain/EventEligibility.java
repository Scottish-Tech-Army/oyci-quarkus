package org.scottishtecharmy.oyci.quarkus.assignment.domain;

import java.util.List;

public record EventEligibility(
        String eventId,
        int requiredStaffCount,
        int currentAssignments,
        List<EligibleStaffCandidate> candidates
) {
}
