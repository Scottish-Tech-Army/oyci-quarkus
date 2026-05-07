package org.scottishtecharmy.oyci.quarkus.masterdata.domain;

public record Location(
        String id,
        String code,
        String name,
        String address,
        String timezone
) {
}
