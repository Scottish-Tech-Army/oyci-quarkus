package org.scottishtecharmy.oyci.quarkus.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.oyci.quarkus.entity.Location;
import org.scottishtecharmy.oyci.quarkus.service.LocationService;

import java.util.List;

@Path("/locations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class LocationResource {

    @Inject
    LocationService locationService;

    @GET
    @RolesAllowed({"ADMIN", "STAFF"})
    public List<Location> list() {
        return locationService.listAll();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "STAFF"})
    public Location get(@PathParam("id") Long id) {
        return locationService.findById(id);
    }

    @POST
    public Response create(Location location) {
        Location created = locationService.create(location);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Location update(@PathParam("id") Long id, Location location) {
        return locationService.update(id, location);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        locationService.delete(id);
        return Response.noContent().build();
    }
}

