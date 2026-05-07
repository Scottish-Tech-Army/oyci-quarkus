package org.scottishtecharmy.oyci.quarkus.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.model.StaffType;

import java.util.Optional;

@ApplicationScoped
public class StaffTypeRepository implements PanacheRepository<StaffType> {

    public Optional<StaffType> findByIdOptional(Long id) {
        return find("staffTypeId", id).firstResultOptional();
    }

    public Optional<StaffType> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}

