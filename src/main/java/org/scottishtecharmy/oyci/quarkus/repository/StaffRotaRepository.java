package org.scottishtecharmy.oyci.quarkus.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.model.StaffRota;

import java.util.List;

@ApplicationScoped
public class StaffRotaRepository implements PanacheRepository<StaffRota> {

    /**
     * Find all rota entries for a given event
     * @param eventId the event ID
     * @return list of staff rota entries
     */
    public List<StaffRota> findByEventId(Long eventId) {
        return list("event.eventId", eventId);
    }

    /**
     * Delete all rota entries for a given event
     * @param eventId the event ID
     * @return number of deleted rows
     */
    public long deleteByEventId(Long eventId) {
        return delete("event.eventId", eventId);
    }
}

