package org.scottishtecharmy.oyci.quarkus.enums;

/**
 * Event Status Enum
 * Defines the lifecycle of an event:
 * 1. DRAFT - Initial state when event is created
 * 2. SCHEDULED - When staff is assigned to the event
 * 3. PUBLISHED - When event is ready and published for registrations
 * 4. COMPLETED - When event has finished
 */
public enum EventStatus {
    DRAFT("draft"),
    SCHEDULED("scheduled"),
    PUBLISHED("published"),
    COMPLETED("completed"),
    DELETED("deleted");

    private final String value;

    EventStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get EventStatus from string value (case-insensitive)
     * @param value Status string
     * @return EventStatus enum
     * @throws IllegalArgumentException if invalid status
     */
    public static EventStatus fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Event status cannot be null");
        }

        for (EventStatus status : EventStatus.values()) {
            if (status.value.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException(
            "Invalid event status: " + value + ". Allowed values are: draft, scheduled, published, completed"
        );
    }

    @Override
    public String toString() {
        return value;
    }
}

