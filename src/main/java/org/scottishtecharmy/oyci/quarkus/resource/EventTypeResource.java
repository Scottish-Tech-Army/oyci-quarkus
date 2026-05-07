package org.scottishtecharmy.oyci.quarkus.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.oyci.quarkus.entity.EventType;
import org.scottishtecharmy.oyci.quarkus.service.EventTypeService;

import java.util.List;

@Path("/event-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class EventTypeResource {

    @Inject
    EventTypeService eventTypeService;

    @GET
    @RolesAllowed({"ADMIN", "STAFF"})
    public List<EventType> list() {
        return eventTypeService.listAll();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "STAFF"})
    public EventType get(@PathParam("id") Long id) {
        return eventTypeService.findById(id);
    }

    @POST
    public Response create(EventType eventType) {
        EventType created = eventTypeService.create(eventType);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public EventType update(@PathParam("id") Long id, EventType eventType) {
        return eventTypeService.update(id, eventType);
    }

    @PUT
    @Path("/{id}/tags")
    public EventType updateTags(@PathParam("id") Long id, List<Long> tagIds) {
        return eventTypeService.updateTags(id, tagIds);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        eventTypeService.delete(id);
        return Response.noContent().build();
    }
}

