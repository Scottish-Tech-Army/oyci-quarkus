package org.scottishtecharmy.oyci.quarkus.resources;

import org.scottishtecharmy.oyci.quarkus.models.CreateEventRequest;
import org.scottishtecharmy.oyci.quarkus.models.ErrorResponse;
import org.scottishtecharmy.oyci.quarkus.models.Event;
import org.scottishtecharmy.oyci.quarkus.services.EventService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource {

    @Inject
    EventService eventService;

    @GET
    public List<Event> getUpcomingEvents() {
        return eventService.getUpcomingEvents();
    }

    @POST
    public Response createEvent(CreateEventRequest request, @Context UriInfo uriInfo) {
        if (request == null || isBlank(request.eventType()) || isBlank(request.eventDate()) || isBlank(request.eventLocation())) {
            return badRequest("eventType, eventDate and eventLocation are required");
        }
        if (!EventService.isValidDate(request.eventDate())) {
            return badRequest("eventDate must be a valid ISO-8601 date or date-time string");
        }

        Event created = eventService.createEvent(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.eventId()).build();
        return Response.created(location).entity(created).build();
    }

    @GET
    @Path("/{eventId}")
    public Response getEventById(@PathParam("eventId") String eventId) {
        if (!isValidUuid(eventId)) {
            return badRequest("eventId must be a valid UUID");
        }

        return eventService.getEventById(eventId)
                .map(Response::ok)
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isValidUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(message))
                .build();
    }
}


