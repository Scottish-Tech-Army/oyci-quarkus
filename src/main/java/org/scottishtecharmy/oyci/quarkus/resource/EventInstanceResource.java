package org.scottishtecharmy.oyci.quarkus.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.oyci.quarkus.dto.AssignRequest;
import org.scottishtecharmy.oyci.quarkus.dto.StaffSuitabilityDTO;
import org.scottishtecharmy.oyci.quarkus.entity.EventAssignment;
import org.scottishtecharmy.oyci.quarkus.entity.EventInstance;
import org.scottishtecharmy.oyci.quarkus.enums.EventStatus;
import org.scottishtecharmy.oyci.quarkus.service.AssignmentEngineService;
import org.scottishtecharmy.oyci.quarkus.service.EventInstanceService;

import java.time.LocalDate;
import java.util.List;

@Path("/event-instances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "STAFF"})
public class EventInstanceResource {

    @Inject
    EventInstanceService eventInstanceService;

    @Inject
    AssignmentEngineService assignmentEngineService;

    @GET
    public List<EventInstance> list(
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("status") String status) {
        LocalDate fromDate = from != null ? LocalDate.parse(from) : null;
        LocalDate toDate = to != null ? LocalDate.parse(to) : null;
        EventStatus statusEnum = status != null ? EventStatus.valueOf(status.toUpperCase()) : null;
        return eventInstanceService.listAll(fromDate, toDate, statusEnum);
    }

    @GET
    @Path("/{id}")
    public EventInstance get(@PathParam("id") Long id) {
        return eventInstanceService.findById(id);
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(EventInstance instance) {
        EventInstance created = eventInstanceService.create(instance);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public EventInstance update(@PathParam("id") Long id, EventInstance instance) {
        return eventInstanceService.update(id, instance);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Long id) {
        eventInstanceService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/available-staff")
    @RolesAllowed("ADMIN")
    public List<StaffSuitabilityDTO> getAvailableStaff(@PathParam("id") Long id) {
        return assignmentEngineService.evaluate(id);
    }

    @POST
    @Path("/{id}/assign")
    @RolesAllowed("ADMIN")
    public Response assignStaff(@PathParam("id") Long id, AssignRequest request) {
        EventAssignment assignment = eventInstanceService.assignStaff(id, request.staffId);
        return Response.status(Response.Status.CREATED).entity(assignment).build();
    }

    @DELETE
    @Path("/{id}/assign/{staffId}")
    @RolesAllowed("ADMIN")
    public Response unassignStaff(@PathParam("id") Long id, @PathParam("staffId") Long staffId) {
        eventInstanceService.unassignStaff(id, staffId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/publish")
    @RolesAllowed("ADMIN")
    public EventInstance publish(@PathParam("id") Long id) {
        return eventInstanceService.publish(id);
    }
}

