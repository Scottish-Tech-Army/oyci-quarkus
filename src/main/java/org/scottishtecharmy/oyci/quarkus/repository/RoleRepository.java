package org.scottishtecharmy.oyci.quarkus.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.model.Role;

@ApplicationScoped
public class RoleRepository implements PanacheRepository<Role> {

    public Role findByRoleId(Long roleId) {
        return find("roleId", roleId).firstResult();
    }

}

