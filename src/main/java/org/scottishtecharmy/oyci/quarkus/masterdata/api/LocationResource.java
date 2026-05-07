package org.scottishtecharmy.oyci.quarkus.masterdata.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.Location;

import java.util.List;

@Path("/locations")
@Produces(MediaType.APPLICATION_JSON)
public class LocationResource {

    @GET
    public List<Location> getLocations() {
        return List.of(
                new Location("loc-1", "GLA_HUB", "Glasgow Hub", "1 George Square, Glasgow", "Europe/London"),
                new Location("loc-2", "EDI_CENTRE", "Edinburgh Centre", "4 Calton Rd, Edinburgh", "Europe/London")
        );
    }
}
