package org.scottishtecharmy.oyci.quarkus.masterdata.domain;

public record StaffMember(
        String id,
        String externalRef,
        String firstName,
        String lastName,
        String email,
        boolean active,
        int weeklyHoursTarget,
        int maxWeeklyHours,
        String timezone
) {
}
