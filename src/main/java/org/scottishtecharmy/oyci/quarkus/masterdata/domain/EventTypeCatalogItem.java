package org.scottishtecharmy.oyci.quarkus.masterdata.domain;

public record EventTypeCatalogItem(
        String id,
        String code,
        String name,
        int defaultDurationMinutes,
        boolean active
) {
}
