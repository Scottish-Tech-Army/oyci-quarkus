package org.scottishtecharmy.oyci.quarkus.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.oyci.quarkus.dto.AvailabilityRequest;
import org.scottishtecharmy.oyci.quarkus.dto.HolidayRequest;
import org.scottishtecharmy.oyci.quarkus.dto.MaxHoursRequest;
import org.scottishtecharmy.oyci.quarkus.entity.*;
import org.scottishtecharmy.oyci.quarkus.service.AuthService;
import org.scottishtecharmy.oyci.quarkus.service.StaffService;

import java.util.List;

@Path("/staff")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "STAFF"})
public class StaffResource {

    @Inject
    StaffService staffService;

    @Inject
    AuthService authService;

    // ===== Admin CRUD =====

    @GET
    @RolesAllowed("ADMIN")
    public List<User> listAll() {
        return staffService.listAllStaff();
    }

    @GET
    @Path("/{id}")
    public User get(@PathParam("id") Long id) {
        return staffService.findById(id);
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(User user) {
        User created = staffService.create(user, authService);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public User update(@PathParam("id") Long id, User user) {
        return staffService.update(id, user);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Long id) {
        staffService.delete(id);
        return Response.noContent().build();
    }

    // ===== Self-Service =====

    @PUT
    @Path("/{id}/availability")
    public Response updateAvailability(@PathParam("id") Long id, List<AvailabilityRequest> windows) {
        staffService.updateAvailability(id, windows);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/max-hours")
    public Response updateMaxHours(@PathParam("id") Long id, MaxHoursRequest request) {
        staffService.updateMaxHours(id, request);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/tags")
    public Response updateTags(@PathParam("id") Long id, List<Long> tagIds) {
        staffService.updateTags(id, tagIds);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/schedule")
    public List<EventAssignment> getSchedule(@PathParam("id") Long id) {
        return staffService.getSchedule(id);
    }

    @GET
    @Path("/{id}/holidays")
    public List<UserHoliday> getHolidays(@PathParam("id") Long id) {
        return staffService.getHolidays(id);
    }

    @POST
    @Path("/{id}/holidays")
    public Response addHoliday(@PathParam("id") Long id, HolidayRequest request) {
        UserHoliday holiday = staffService.addHoliday(id, request);
        return Response.status(Response.Status.CREATED).entity(holiday).build();
    }

    @DELETE
    @Path("/{id}/holidays/{holidayId}")
    public Response deleteHoliday(@PathParam("id") Long id, @PathParam("holidayId") Long holidayId) {
        staffService.deleteHoliday(id, holidayId);
        return Response.noContent().build();
    }
}

