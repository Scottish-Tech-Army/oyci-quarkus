package org.scottishtecharmy.oyci.quarkus;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.logging.Logger;
import org.scottishtecharmy.oyci.quarkus.request.ApplyLeaveRequest;
import org.scottishtecharmy.oyci.quarkus.response.LeaveResponse;
import org.scottishtecharmy.oyci.quarkus.service.LeaveService;

import java.net.URI;
import java.util.List;

@Path("/leave")
@Produces(MediaType.APPLICATION_JSON)
public class LeaveResource {

    private static final Logger LOG = Logger.getLogger(LeaveResource.class);

    @Inject
    LeaveService leaveService;

    /**
     * Apply for leave
     * POST /leave/apply
     *
     * Request Body Example:
     * {
     *   "staffId": 1,
     *   "startDatetime": "2026-04-10T09:00:00+01:00",
     *   "endDatetime": "2026-04-12T17:00:00+01:00",
     *   "leaveType": "ANNUAL",
     *   "reason": "Family holiday"
     * }
     */
    @POST
    @Path("/apply")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response applyLeave(@Valid ApplyLeaveRequest request) {
        LOG.infof("POST /leave/apply - staffId=%d, type=%s", request.getStaffId(), request.getLeaveType());

        LeaveResponse leaveResponse = leaveService.applyLeave(request);

        URI location = UriBuilder.fromResource(LeaveResource.class)
                .path("/staff/{staffId}")
                .build(request.getStaffId());

        LOG.infof("POST /leave/apply - Leave created with id=%d", leaveResponse.getId());
        return Response.created(location).entity(leaveResponse).build();
    }

    /**
     * Get all leave requests for a specific staff member
     * GET /leave/staff/{staffId}
     */
    @GET
    @Path("/staff/{staffId}")
    public Response getLeaveByStaff(@PathParam("staffId") Long staffId) {
        LOG.infof("GET /leave/staff/%d - Fetching leave for staff", staffId);
        List<LeaveResponse> leaves = leaveService.getLeaveByStaff(staffId);
        LOG.infof("GET /leave/staff/%d - Returned %d records", staffId, leaves.size());
        return Response.ok(leaves).build();
    }

    /**
     * Get all leave requests (admin)
     * GET /leave/list-leaves
     */
    @GET
    @Path("/list-leaves")
    public Response getAllLeave() {
        LOG.info("GET /leave/list-leaves - Fetching all leave requests");
        List<LeaveResponse> leaves = leaveService.getAllLeave();
        LOG.infof("GET /leave/list-leaves - Returned %d records", leaves.size());
        return Response.ok(leaves).build();
    }

    /**
     * Update a leave request (only if not yet approved)
     * PUT /leave/{id}
     *
     * Request Body Example:
     * {
     *   "staffId": 1,
     *   "startDatetime": "2026-05-02T09:00:00+00:00",
     *   "endDatetime": "2026-05-06T17:00:00+00:00",
     *   "leaveType": "SICK",
     *   "reason": "Updated reason"
     * }
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLeave(@PathParam("id") Long leaveId, @Valid ApplyLeaveRequest request) {
        LOG.infof("PUT /leave/%d - Updating leave", leaveId);
        LeaveResponse updated = leaveService.updateLeave(leaveId, request);
        LOG.infof("PUT /leave/%d - Leave updated successfully", leaveId);
        return Response.ok(updated).build();
    }

    /**
     * Cancel a leave request
     * PUT /leave/{id}/cancel
     */
    @PUT
    @Path("/{id}/cancel")
    @Consumes(MediaType.WILDCARD)
    public Response cancelLeave(@PathParam("id") Long leaveId) {
        LOG.infof("PUT /leave/%d/cancel - Cancelling leave", leaveId);
        LeaveResponse cancelled = leaveService.cancelLeave(leaveId);
        LOG.infof("PUT /leave/%d/cancel - Leave cancelled successfully", leaveId);
        return Response.ok(cancelled).build();
    }
}

