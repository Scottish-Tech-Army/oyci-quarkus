package org.scottishtecharmy.oyci.quarkus.assignment.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.scottishtecharmy.oyci.quarkus.assignment.application.AssignmentScaffoldStore;
import org.scottishtecharmy.oyci.quarkus.assignment.domain.EventStaffAssignment;

import java.util.List;

@Path("/assignments")
@Produces(MediaType.APPLICATION_JSON)
public class AssignmentResource {

    private final AssignmentScaffoldStore assignmentStore;

    public AssignmentResource(AssignmentScaffoldStore assignmentStore) {
        this.assignmentStore = assignmentStore;
    }

    @GET
    public List<EventStaffAssignment> getAssignments() {
        return assignmentStore.findAll();
    }
}
