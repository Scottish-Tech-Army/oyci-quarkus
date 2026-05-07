package org.scottishtecharmy.oyci.quarkus;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.scottishtecharmy.oyci.quarkus.repository.StaffTypeRepository;
import org.scottishtecharmy.oyci.quarkus.response.StaffTypeResponse;

import java.util.List;
import java.util.stream.Collectors;

@Path("/staff-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StaffTypeResource {

    private static final Logger LOG = Logger.getLogger(StaffTypeResource.class);

    @Inject
    StaffTypeRepository staffTypeRepository;

    /**
     * List all staff types
     * GET /staff-types
     */
    @GET
    public List<StaffTypeResponse> listStaffTypes() {
        LOG.info("GET /staff-types - Fetching all staff types");
        return staffTypeRepository.listAll()
                .stream()
                .map(StaffTypeResponse::from)
                .collect(Collectors.toList());
    }
}

