package org.scottishtecharmy.oyci.quarkus.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.model.EventType;

import java.util.Optional;

@ApplicationScoped
public class EventTypeRepository implements PanacheRepository<EventType> {

    public Optional<EventType> findByIdOptional(Long id) {
        return find("eventTypeId", id).firstResultOptional();
    }

    public Optional<EventType> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}

