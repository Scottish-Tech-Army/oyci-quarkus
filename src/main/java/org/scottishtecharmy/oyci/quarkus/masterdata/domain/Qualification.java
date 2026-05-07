package org.scottishtecharmy.oyci.quarkus.masterdata.domain;

public record Qualification(
        String id,
        String code,
        String name,
        boolean active
) {
}
