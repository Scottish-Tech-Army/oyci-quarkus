package org.scottishtecharmy.oyci.quarkus.scheduling.domain;

public record SessionEvent(
        String id,
        String schedulePeriodId,
        String eventTypeId,
        String locationId,
        String startAt,
        String endAt,
        int requiredStaffCount,
        SessionEventStatus status,
        String notes,
        String sourceRowRef
) {
}
