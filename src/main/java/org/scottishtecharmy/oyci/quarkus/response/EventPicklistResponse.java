package org.scottishtecharmy.oyci.quarkus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response for event picklist containing event types and locations
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventPicklistResponse {

    private List<PicklistItem> eventTypes;
    private List<PicklistItem> locations;

    /**
     * Simple picklist item with id and name
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PicklistItem {
        private Long id;
        private String name;
    }
}

