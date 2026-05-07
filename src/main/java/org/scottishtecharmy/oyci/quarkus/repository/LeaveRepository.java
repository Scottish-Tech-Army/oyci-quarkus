package org.scottishtecharmy.oyci.quarkus.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.model.Leave;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LeaveRepository implements PanacheRepository<Leave> {

    public List<Leave> findByStaffId(Long staffId) {
        return list("staff.staffId = ?1 and status != 'CANCELLED'", staffId);
    }

    public List<Leave> findAllActive() {
        return list("status != 'CANCELLED'");
    }

    public Optional<Leave> findActiveById(Long id) {
        return find("id = ?1 and status != 'CANCELLED'", id).firstResultOptional();
    }
}

