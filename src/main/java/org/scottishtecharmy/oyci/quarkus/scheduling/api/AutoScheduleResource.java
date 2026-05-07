package org.scottishtecharmy.oyci.quarkus.scheduling.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.oyci.quarkus.scheduling.application.AutoScheduleService;
import org.scottishtecharmy.oyci.quarkus.scheduling.domain.AutoScheduleResult;

@Path("/schedule")
@Produces(MediaType.APPLICATION_JSON)
public class AutoScheduleResource {

    @Inject
    AutoScheduleService autoScheduleService;

    @POST
    @Path("/auto")
    public Response runAutoSchedule() {
        AutoScheduleResult result = autoScheduleService.scheduleAll();
        return Response.ok(result).build();
    }
}
