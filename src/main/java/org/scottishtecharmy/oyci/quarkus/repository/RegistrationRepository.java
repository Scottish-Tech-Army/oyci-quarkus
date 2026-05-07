package org.scottishtecharmy.oyci.quarkus.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.model.Registration;

@ApplicationScoped
public class RegistrationRepository implements PanacheRepository<Registration> {

    /**
     * Count approved registrations for a specific event
     * @param eventId Event ID
     * @return Count of approved registrations
     */
    public long countApprovedByEventId(Long eventId) {
        return count("event.eventId = ?1 and status = ?2", eventId, "APPROVED");
    }

    /**
     * Count all approved attendees for events with a specific status
     * @param eventStatus Event status (e.g., "completed")
     * @return Total count of approved registrations for events with the given status
     */
    public long countApprovedByEventStatus(String eventStatus) {
        return count("LOWER(event.status) = LOWER(?1) and status = ?2", eventStatus, "APPROVED");
    }
}

