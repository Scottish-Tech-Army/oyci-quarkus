package org.scottishtecharmy.oyci.quarkus.masterdata.application;

import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.Qualification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Single source of truth for the qualification catalogue.
 * Injected wherever qualification data is needed to avoid duplication.
 */
@ApplicationScoped
public class QualificationRegistry {

    private static final Map<String, Qualification> BY_ID = Map.of(
            "qual-1", new Qualification("qual-1", "SAFEGUARD_L1", "Safeguarding Level 1", true),
            "qual-2", new Qualification("qual-2", "FIRST_AID", "Emergency First Aid", true)
    );

    public List<Qualification> getAll() {
        return List.copyOf(BY_ID.values());
    }

    public Optional<Qualification> findById(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }
}
