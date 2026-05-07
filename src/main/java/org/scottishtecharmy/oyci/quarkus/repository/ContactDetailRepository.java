package org.scottishtecharmy.oyci.quarkus.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.model.ContactDetail;

import java.util.Optional;

@ApplicationScoped
public class ContactDetailRepository implements PanacheRepository<ContactDetail> {

    public Optional<ContactDetail> findByIdOptional(Long id) {
        return find("contactDetailId", id).firstResultOptional();
    }

    public Optional<ContactDetail> findByPrimaryEmail(String email) {
        return find("primaryEmail", email).firstResultOptional();
    }
}

