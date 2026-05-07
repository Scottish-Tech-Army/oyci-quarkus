package org.scottishtecharmy.oyci.quarkus.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.model.Location;

import java.util.Optional;

@ApplicationScoped
public class LocationRepository implements PanacheRepository<Location> {

    public Optional<Location> findByIdOptional(Long id) {
        return find("locationId", id).firstResultOptional();
    }

    public Optional<Location> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}
