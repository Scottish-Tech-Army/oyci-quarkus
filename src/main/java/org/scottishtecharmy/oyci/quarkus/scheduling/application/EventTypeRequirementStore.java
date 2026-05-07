package org.scottishtecharmy.oyci.quarkus.scheduling.application;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Set;

/**
 * Maps event type codes to the qualification IDs required to staff them.
 * TRAINING  → Safeguarding Level 1 (qual-1)
 * MATCHDAY  → Safeguarding Level 1 (qual-1) + Emergency First Aid (qual-2)
 */
@ApplicationScoped
public class EventTypeRequirementStore {

    private static final Map<String, Set<String>> REQUIREMENTS = Map.of(
            "TRAINING", Set.of("qual-1"),
            "MATCHDAY", Set.of("qual-1", "qual-2")
    );

    /** Returns the set of qualification IDs required for an event type code. Empty set = no requirements. */
    public Set<String> getRequiredQualificationIds(String eventTypeCode) {
        if (eventTypeCode == null) return Set.of();
        return REQUIREMENTS.getOrDefault(eventTypeCode.toUpperCase(), Set.of());
    }
}
