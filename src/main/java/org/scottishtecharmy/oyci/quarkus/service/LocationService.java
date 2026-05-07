package org.scottishtecharmy.oyci.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.scottishtecharmy.oyci.quarkus.entity.Location;

import java.util.List;

@ApplicationScoped
public class LocationService {

    public List<Location> listAll() {
        return Location.listAll();
    }

    public Location findById(Long id) {
        Location loc = Location.findById(id);
        if (loc == null) throw new NotFoundException("Location not found");
        return loc;
    }

    @Transactional
    public Location create(Location location) {
        location.persist();
        return location;
    }

    @Transactional
    public Location update(Long id, Location updated) {
        Location loc = Location.findById(id);
        if (loc == null) throw new NotFoundException("Location not found");
        loc.name = updated.name;
        loc.addressLine1 = updated.addressLine1;
        loc.addressLine2 = updated.addressLine2;
        loc.city = updated.city;
        loc.zipCode = updated.zipCode;
        loc.contactName = updated.contactName;
        loc.contactPhone = updated.contactPhone;
        loc.contactEmail = updated.contactEmail;
        loc.defaultCapacity = updated.defaultCapacity;
        return loc;
    }

    @Transactional
    public void delete(Long id) {
        Location loc = Location.findById(id);
        if (loc == null) throw new NotFoundException("Location not found");
        loc.delete();
    }
}

