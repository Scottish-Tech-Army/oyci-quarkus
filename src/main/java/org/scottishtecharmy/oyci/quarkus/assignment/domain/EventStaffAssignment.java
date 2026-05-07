package org.scottishtecharmy.oyci.quarkus.assignment.domain;

public record EventStaffAssignment(
        String id,
        String sessionEventId,
        String staffId,
        String role,
        AssignmentStatus status,
        String assignedAt,
        String assignedBy,
        double hoursCommittedSnapshot
) {
}
