package org.scottishtecharmy.oyci.quarkus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response wrapper for list-events endpoint
 * Contains all events, upcoming events count, and total attendees for completed events
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventListResponse {

    /**
     * List of all events with their details
     */
    private List<EventResponse> eventsInfo;

    /**
     * Count of events with status = "scheduled"
     */
    private long upcomingEvent;

    /**
     * Total count of all approved attendees across events with status = "completed"
     */
    private long totalattendes;
}

