package org.scottishtecharmy.oyci.quarkus.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.scottishtecharmy.oyci.quarkus.entity.EventInstance;
import org.scottishtecharmy.oyci.quarkus.service.ParticipantService;

import java.util.List;

@Path("/participant")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"PARTICIPANT", "ADMIN"})
public class ParticipantResource {

    @Inject
    ParticipantService participantService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/events")
    public List<EventInstance> listPublishedEvents() {
        return participantService.listPublishedEvents();
    }

    @POST
    @Path("/events/{id}/register")
    public Response register(@PathParam("id") Long eventId) {
        Long participantId = Long.parseLong(jwt.getSubject());
        participantService.register(eventId, participantId);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/events/{id}/register")
    public Response cancelRegistration(@PathParam("id") Long eventId) {
        Long participantId = Long.parseLong(jwt.getSubject());
        participantService.cancelRegistration(eventId, participantId);
        return Response.noContent().build();
    }
}

