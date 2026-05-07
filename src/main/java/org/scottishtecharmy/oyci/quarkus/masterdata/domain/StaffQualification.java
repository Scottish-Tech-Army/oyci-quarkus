package org.scottishtecharmy.oyci.quarkus.masterdata.domain;

public record StaffQualification(
        String staffId,
        String qualificationId,
        String level,
        String validUntil
) {
}
