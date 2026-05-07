package org.scottishtecharmy.oyci.quarkus.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.model.Qualification;

import java.util.Optional;

@ApplicationScoped
public class QualificationRepository implements PanacheRepository<Qualification> {

    public Optional<Qualification> findByIdOptional(Long id) {
        return find("qualificationId", id).firstResultOptional();
    }

}

