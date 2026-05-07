package org.scottishtecharmy.oyci.quarkus.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.model.User;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByIdOptional(Long id) {
        return find("userId", id).firstResultOptional();
    }

    public User findByEmail(String email) {
        return find("contactDetail.primaryEmail", email).firstResult();
    }

    /**
     * Find all active admin users
     */
    public List<User> findAdminUsers() {
        return list("roleId.roleType = 'ADMIN' and isActive = true");
    }
}
