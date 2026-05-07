package org.scottishtecharmy.oyci.quarkus;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.scottishtecharmy.oyci.quarkus.dto.CreateStaffRequest;
import org.scottishtecharmy.oyci.quarkus.dto.UpdateStaffRequest;
import org.scottishtecharmy.oyci.quarkus.repository.StaffRepository;
import org.scottishtecharmy.oyci.quarkus.response.StaffDetailResponse;
import org.scottishtecharmy.oyci.quarkus.response.StaffListResponse;
import org.scottishtecharmy.oyci.quarkus.service.StaffService;

import java.util.List;
import java.util.stream.Collectors;

@Path("/staff")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StaffResource {

    private static final Logger LOG = Logger.getLogger(StaffResource.class);

    @Inject
    StaffRepository staffRepository;

    @Inject
    StaffService staffService;

    /**
     * List all staff members with active staff count
     * GET /staff/list-staff
     */
    @GET
    @Path("/list-staff")
    public StaffListResponse listStaff() {
        LOG.info("GET /staff/list-staff - Fetching all staff");

        List<StaffDetailResponse> staffList = staffRepository.listAll()
                .stream()
                .map(StaffDetailResponse::from)
                .collect(Collectors.toList());

        // Count active staff (isActive = true)
        long activeStaffCount = staffList.stream()
                .filter(staff -> staff.getIsActive() != null && staff.getIsActive())
                .count();

        return new StaffListResponse(staffList, activeStaffCount);
    }

    /**
     * Get a single staff member by ID
     * GET /staff/{staffId}
     */
    @GET
    @Path("/{staffId}")
    public Response getStaff(@PathParam("staffId") Long staffId) {
        LOG.infof("GET /staff/%d - Fetching staff by ID", staffId);
        StaffDetailResponse staff = staffService.getStaffById(staffId);
        return Response.ok(staff).build();
    }

    /**
     * Create a new staff member
     * POST /staff
     */
    @POST
    @Path("/create-staff")
    public Response createStaff(@Valid CreateStaffRequest request) {
        LOG.info("POST /staff - Creating new staff member");
        StaffDetailResponse created = staffService.createStaff(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Update an existing staff member
     * PUT /staff/{staffId}
     */
    @PUT
    @Path("/{staffId}")
    public Response updateStaff(
            @PathParam("staffId") Long staffId,
            @Valid UpdateStaffRequest request,
            @HeaderParam("X-User-Id") String userId) {

        LOG.infof("PUT /staff/%d - Updating staff", staffId);

        String updatedBy = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";
        StaffDetailResponse updated = staffService.updateStaff(staffId, request, updatedBy);

        return Response.ok(updated).build();
    }

    /**
     * Delete (deactivate) a staff member by setting isActive to false
     * DELETE /staff/{staffId}
     */
    @DELETE
    @Path("/{staffId}")
    public Response deleteStaff(
            @PathParam("staffId") Long staffId,
            @HeaderParam("X-User-Id") String userId) {

        LOG.infof("DELETE /staff/%d - Deactivating staff", staffId);

        String deletedBy = (userId != null && !userId.isEmpty()) ? userId : "SYSTEM";
        staffService.deleteStaff(staffId, deletedBy);

        return Response.noContent().build();
    }
}
