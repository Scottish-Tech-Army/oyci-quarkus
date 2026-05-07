package org.scottishtecharmy.oyci.quarkus.masterdata.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.oyci.quarkus.masterdata.application.UnavailabilityStore;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.CreateUnavailabilityRequest;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.UnavailabilityPeriod;
import org.scottishtecharmy.oyci.quarkus.models.ErrorResponse;

import java.util.List;

@Path("/staff/{staffId}/unavailability")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StaffUnavailabilityResource {

    @Inject
    UnavailabilityStore unavailabilityStore;

    @GET
    public List<UnavailabilityPeriod> getUnavailability(@PathParam("staffId") String staffId) {
        return unavailabilityStore.findByStaffId(staffId);
    }

    @POST
    public Response addUnavailability(
            @PathParam("staffId") String staffId,
            CreateUnavailabilityRequest request
    ) {
        if (request == null || isBlank(request.startDate()) || isBlank(request.endDate())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("startDate and endDate are required (YYYY-MM-DD)"))
                    .build();
        }
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(request.startDate());
            java.time.LocalDate end   = java.time.LocalDate.parse(request.endDate());
            if (end.isBefore(start)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("endDate must not be before startDate"))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Dates must be in YYYY-MM-DD format"))
                    .build();
        }

        UnavailabilityPeriod created = unavailabilityStore.add(
                staffId, request.startDate(), request.endDate(), request.reason()
        );
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @DELETE
    @Path("/{periodId}")
    public Response deleteUnavailability(
            @PathParam("staffId") String staffId,
            @PathParam("periodId") String periodId
    ) {
        boolean deleted = unavailabilityStore.delete(staffId, periodId);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Unavailability period not found"))
                    .build();
        }
        return Response.noContent().build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
