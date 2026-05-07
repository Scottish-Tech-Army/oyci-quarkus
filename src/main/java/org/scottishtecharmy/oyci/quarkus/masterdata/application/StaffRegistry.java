package org.scottishtecharmy.oyci.quarkus.masterdata.application;

import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.StaffMember;

import java.util.List;
import java.util.Optional;

/**
 * Single source of truth for the staff list.
 * Injected wherever staff data is needed to avoid duplication.
 */
@ApplicationScoped
public class StaffRegistry {

    private static final List<StaffMember> ALL_STAFF = List.of(
            new StaffMember("staff-1", "ZP-102", "Aileen", "Campbell", "aileen.campbell@example.org", true, 20, 30, "Europe/London"),
            new StaffMember("staff-2", "ZP-221", "Gregor", "Macleod", "gregor.macleod@example.org", true, 16, 24, "Europe/London")
    );

    public List<StaffMember> getAll() {
        return ALL_STAFF;
    }

    public List<StaffMember> getActive() {
        return ALL_STAFF.stream().filter(StaffMember::active).toList();
    }

    public Optional<StaffMember> findById(String id) {
        return ALL_STAFF.stream().filter(s -> s.id().equals(id)).findFirst();
    }
}
