package org.scottishtecharmy.oyci.quarkus.masterdata.domain;

public record CreateUnavailabilityRequest(
        String startDate,  // YYYY-MM-DD
        String endDate,    // YYYY-MM-DD
        String reason
) {
}
