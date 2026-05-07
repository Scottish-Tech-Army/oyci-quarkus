package org.scottishtecharmy.oyci.quarkus.assignment.domain;

public record CreateAssignmentRequest(
        String staffId,
        String role,
        String assignedBy
) {
}
