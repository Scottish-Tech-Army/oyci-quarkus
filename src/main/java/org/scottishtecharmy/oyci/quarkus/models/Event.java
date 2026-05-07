package org.scottishtecharmy.oyci.quarkus.models;

public record Event(
        String eventId,
        String eventType,
        String eventDate,
        String eventLocation
) {
}


