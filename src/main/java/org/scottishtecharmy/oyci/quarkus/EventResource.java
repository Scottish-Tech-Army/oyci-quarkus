package org.scottishtecharmy.oyci.quarkus;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.logging.Logger;
import org.scottishtecharmy.oyci.quarkus.dto.CreateEventRequest;
import org.scottishtecharmy.oyci.quarkus.dto.UpdateEventRequest;
import org.scottishtecharmy.oyci.quarkus.response.EventListResponse;
import org.scottishtecharmy.oyci.quarkus.response.EventPicklistResponse;
import org.scottishtecharmy.oyci.quarkus.response.EventResponse;
import org.scottishtecharmy.oyci.quarkus.service.EventService;

import java.net.URI;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource {

    private static final Logger LOG = Logger.getLogger(EventResource.class);

    @Inject
    EventService eventService;

    /**
     * Get all events with statistics
     * GET /events/list-events
     *
     * Response format:
     * {
     *   "eventsInfo": [...],           // Array of all events
     *   "upcomingEvent": 6,            // Count of events with status="scheduled"
     *   "totalattendes": 10            // Total approved attendees for "completed" events
     * }
     */
    @GET
    @Path("/list-events")
    public Response listEvents() {
        LOG.info("GET /events/list-events - Fetching all events with statistics");
        EventListResponse response = eventService.getAllEvents();
        return Response.ok(response).build();
    }

    /**
     * Get event by ID
     * GET /events/{id}
     */
    @GET
    @Path("/{id}")
    public Response getEventById(@PathParam("id") Long eventId) {
        LOG.infof("GET /events/%d - Fetching event by ID", eventId);
        EventResponse event = eventService.getEventById(eventId);
        return Response.ok(event).build();
    }

    /**
     * Get event picklist data (event types and locations)
     * GET /events/get-event-picklist
     *
     * Response format:
     * {
     *   "eventTypes": [
     *     { "id": 1, "name": "Community" },
     *     { "id": 2, "name": "Training" }
     *   ],
     *   "locations": [
     *     { "id": 10, "name": "Glasgow City Hall" },
     *     { "id": 11, "name": "Edinburgh Conference Centre" }
     *   ]
     * }
     */
    @GET
    @Path("/get-event-picklist")
    public Response getEventPicklist() {
        LOG.info("GET /events/get-event-picklist - Fetching picklist data");
        EventPicklistResponse picklist = eventService.getEventPicklist();
        return Response.ok(picklist).build();
    }

    /**
     * Create a new event
     * POST /events/create-event
     *
     * Headers:
     *   X-User-Id: User ID of the event creator (optional, defaults to SYSTEM)
     *
     * Request Body Example:
     * {
     *   "eventName": "Community Cleanup Day",
     *   "eventType": "Community",
     *   "description": "Join us for a community cleanup event",
     *   "eventDate": "15/04/2026",
     *   "startTime": "09:00",
     *   "endTime": "17:00",
     *   "location": "Glasgow City Hall",
     *   "maxAttendees": 50
     * }
     */
    @POST
    @Path("/create-event")
    public Response createEvent(
            @Valid CreateEventRequest request,
            @HeaderParam("X-User-Id") String userId) {

        LOG.infof("POST /events/create-event - Creating new event: %s", request.getEventName());

        // Use userId from header if available, otherwise use SYSTEM
        String createdBy = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";

        EventResponse createdEvent = eventService.createEvent(request, createdBy);

        // Build location URI for the created resource
        URI location = UriBuilder.fromResource(EventResource.class)
                .path("/{id}")
                .build(createdEvent.getEventId());

        return Response.created(location)
                .entity(createdEvent)
                .build();
    }

    /**
     * Update an existing event and manage staff rota assignments
     * PUT /events/update-event/{id}
     *
     * Headers:
     *   X-User-Id: User ID of the person updating (optional, defaults to SYSTEM)
     *
     * Request Body Example:
     * {
     *   "eventName": "ASC Session",
     *   "eventType": "Youth Activity",
     *   "description": "After school club",
     *   "eventDate": "05/01/2026",
     *   "startTime": "15:30",
     *   "endTime": "17:30",
     *   "location": "West Hall",
     *   "maxAttendees": 20,
     *   "status": "scheduled",
     *   "shiftStart": "14:00",
     *   "shiftEnd": "18:00",
     *   "staffAssignments": [
     *     { "staffId": 3, "role": "Lead", "shiftStart": "14:00", "shiftEnd": "18:00" },
     *     { "staffId": 7, "role": "Support", "shiftStart": "15:00", "shiftEnd": "18:00" }
     *   ]
     * }
     */
    @PUT
    @Path("/update-event/{id}")
    public Response updateEvent(
            @PathParam("id") Long eventId,
            @Valid UpdateEventRequest request,
            @HeaderParam("X-User-Id") String userId) {

        LOG.infof("PUT /events/update-event/%d - Updating event: %s", eventId, request.getEventName());

        String updatedBy = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";

        EventResponse updatedEvent = eventService.updateEvent(eventId, request, updatedBy);

        return Response.ok(updatedEvent).build();
    }

    /**
     * Soft delete an event by setting its status to DELETED
     * PUT /events/delete-event/{id}
     */
    @PUT
    @Path("/delete-event/{id}")
    public Response deleteEvent(@PathParam("id") Long eventId) {
        LOG.infof("DELETE /events/delete-event/%d - Soft deleting event", eventId);
        eventService.deleteEvent(eventId);
        return Response.noContent().build();
    }
}

