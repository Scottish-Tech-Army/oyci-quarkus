package org.scottishtecharmy.oyci.quarkus.assignment.domain;

public record EligibleStaffCandidate(
        String staffId,
        String displayName,
        boolean eligible,
        String reason
) {
}
