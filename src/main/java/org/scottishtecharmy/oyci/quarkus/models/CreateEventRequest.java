package org.scottishtecharmy.oyci.quarkus.models;

public record CreateEventRequest(
        String eventType,
        String eventDate,
        String eventLocation
) {
}
