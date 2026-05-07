package org.scottishtecharmy.oyci.quarkus.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.oyci.quarkus.dto.NotifyStaffRequest;
import org.scottishtecharmy.oyci.quarkus.service.CommunicationsService;

import java.util.Map;

@Path("/communications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class CommunicationsResource {

    @Inject
    CommunicationsService communicationsService;

    @POST
    @Path("/notify-staff")
    public Response notifyStaff(NotifyStaffRequest request) {
        int sent = communicationsService.notifyStaff(request.fromDate, request.toDate);
        return Response.ok(Map.of("emailsSent", sent)).build();
    }
}

