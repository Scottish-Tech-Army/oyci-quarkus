package org.scottishtecharmy.oyci.quarkus.scheduling.domain;

import org.scottishtecharmy.oyci.quarkus.assignment.domain.EventStaffAssignment;

import java.util.List;

public record AutoScheduleResult(
        int scheduledCount,
        int skippedCount,
        String message,
        List<EventStaffAssignment> assignments
) {
}
