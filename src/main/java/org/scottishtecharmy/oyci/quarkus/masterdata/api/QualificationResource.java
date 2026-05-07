package org.scottishtecharmy.oyci.quarkus.masterdata.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.scottishtecharmy.oyci.quarkus.masterdata.application.QualificationRegistry;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.Qualification;

import java.util.List;

@Path("/qualifications")
@Produces(MediaType.APPLICATION_JSON)
public class QualificationResource {

    @Inject
    QualificationRegistry qualificationRegistry;

    @GET
    public List<Qualification> getQualifications() {
        return qualificationRegistry.getAll();
    }
}
