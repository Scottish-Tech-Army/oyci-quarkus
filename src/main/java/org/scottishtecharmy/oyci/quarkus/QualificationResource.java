package org.scottishtecharmy.oyci.quarkus;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.scottishtecharmy.oyci.quarkus.repository.QualificationRepository;
import org.scottishtecharmy.oyci.quarkus.response.QualificationResponse;

import java.util.List;
import java.util.stream.Collectors;

@Path("/qualifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QualificationResource {

    private static final Logger LOG = Logger.getLogger(QualificationResource.class);

    @Inject
    QualificationRepository qualificationRepository;

    /**
     * List all qualifications
     * GET /qualifications
     */
    @GET
    public List<QualificationResponse> listQualifications() {
        LOG.info("GET /qualifications - Fetching all qualifications");
        return qualificationRepository.listAll()
                .stream()
                .map(QualificationResponse::from)
                .collect(Collectors.toList());
    }
}

