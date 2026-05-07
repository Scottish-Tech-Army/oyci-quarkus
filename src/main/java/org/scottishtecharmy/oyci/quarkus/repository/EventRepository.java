package org.scottishtecharmy.oyci.quarkus.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.enums.EventStatus;
import org.scottishtecharmy.oyci.quarkus.model.Event;

import java.util.List;

@ApplicationScoped
public class EventRepository implements PanacheRepository<Event> {

    /**
     * Count events by status
     * @param status Event status enum
     * @return Count of events with the given status
     */
    public long countByStatus(EventStatus status) {
        return count("status = ?1", status);
    }

    /**
     * Find all events by status
     * @param status Event status enum
     * @return List of events with the given status
     */
    public List<Event> findByStatus(EventStatus status) {
        return list("status = ?1", status);
    }
}

