package org.scottishtecharmy.oyci.quarkus.masterdata.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.scottishtecharmy.oyci.quarkus.masterdata.application.QualificationRegistry;
import org.scottishtecharmy.oyci.quarkus.masterdata.application.StaffQualificationStore;
import org.scottishtecharmy.oyci.quarkus.masterdata.application.StaffRegistry;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.Qualification;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.StaffMember;

import java.util.List;
import java.util.Set;

@Path("/staff")
@Produces(MediaType.APPLICATION_JSON)
public class StaffResource {

    @Inject StaffRegistry staffRegistry;
    @Inject StaffQualificationStore qualificationStore;
    @Inject QualificationRegistry qualificationRegistry;

    @GET
    public List<StaffMember> getStaff() {
        return staffRegistry.getAll();
    }

    @GET
    @Path("/{staffId}/qualifications")
    public List<Qualification> getStaffQualifications(@PathParam("staffId") String staffId) {
        Set<String> qualIds = qualificationStore.getQualificationIdsForStaff(staffId);
        return qualIds.stream()
                .map(qualificationRegistry::findById)
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}
