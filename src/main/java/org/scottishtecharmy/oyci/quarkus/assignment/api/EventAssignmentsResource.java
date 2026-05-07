package org.scottishtecharmy.oyci.quarkus.assignment.api;

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
import org.scottishtecharmy.oyci.quarkus.assignment.application.AssignmentScaffoldStore;
import org.scottishtecharmy.oyci.quarkus.assignment.domain.AssignmentStatus;
import org.scottishtecharmy.oyci.quarkus.assignment.domain.CreateAssignmentRequest;
import org.scottishtecharmy.oyci.quarkus.assignment.domain.EventStaffAssignment;
import org.scottishtecharmy.oyci.quarkus.models.ErrorResponse;
import org.scottishtecharmy.oyci.quarkus.scheduling.application.AutoScheduleService;

import java.net.URI;
import java.util.List;

@Path("/events/{eventId}/assignments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventAssignmentsResource {

    private final AssignmentScaffoldStore assignmentStore;

    @Inject
    AutoScheduleService autoScheduleService;

    public EventAssignmentsResource(AssignmentScaffoldStore assignmentStore) {
        this.assignmentStore = assignmentStore;
    }

    @GET
    public List<EventStaffAssignment> getEventAssignments(@PathParam("eventId") String eventId) {
        return assignmentStore.findByEventId(eventId);
    }

    @POST
    public Response createAssignment(
            @PathParam("eventId") String eventId,
            CreateAssignmentRequest request,
            @Context UriInfo uriInfo
    ) {
        if (request == null || isBlank(request.staffId()) || isBlank(request.assignedBy())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("staffId and assignedBy are required"))
                    .build();
        }
        EventStaffAssignment created = assignmentStore.add(eventId, request.staffId(), request.role(), request.assignedBy());
        URI location = uriInfo.getAbsolutePathBuilder().path(created.id()).build();
        return Response.created(location).entity(created).build();
    }

    @POST
    @Path("/{assignmentId}/accept")
    public Response acceptAssignment(
            @PathParam("eventId") String eventId,
            @PathParam("assignmentId") String assignmentId
    ) {
        return assignmentStore.updateStatus(assignmentId, AssignmentStatus.CONFIRMED)
                .map(a -> Response.ok(a).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Assignment not found: " + assignmentId))
                        .build());
    }

    @POST
    @Path("/{assignmentId}/decline")
    public Response declineAssignment(
            @PathParam("eventId") String eventId,
            @PathParam("assignmentId") String assignmentId
    ) {
        return assignmentStore.updateStatus(assignmentId, AssignmentStatus.CANCELLED)
                .map(cancelled -> {
                    // Trigger re-scheduling to fill the gap left by the declining staff member
                    autoScheduleService.scheduleEvent(eventId);
                    return Response.ok(cancelled).build();
                })
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Assignment not found: " + assignmentId))
                        .build());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

