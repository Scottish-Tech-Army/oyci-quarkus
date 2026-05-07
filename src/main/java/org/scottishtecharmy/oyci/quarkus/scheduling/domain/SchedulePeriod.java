package org.scottishtecharmy.oyci.quarkus.scheduling.domain;

public record SchedulePeriod(
        String id,
        String name,
        String season,
        int year,
        String startDate,
        String endDate,
        SchedulePeriodStatus status
) {
}
