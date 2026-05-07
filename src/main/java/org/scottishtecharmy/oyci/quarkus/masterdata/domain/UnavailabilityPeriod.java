package org.scottishtecharmy.oyci.quarkus.masterdata.domain;

public record UnavailabilityPeriod(
        String id,
        String staffId,
        String startDate,  // YYYY-MM-DD inclusive
        String endDate,    // YYYY-MM-DD inclusive
        String reason
) {
}
