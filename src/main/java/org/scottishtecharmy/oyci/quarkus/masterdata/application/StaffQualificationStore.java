package org.scottishtecharmy.oyci.quarkus.masterdata.application;

import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.StaffQualification;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class StaffQualificationStore {

    private final List<StaffQualification> records = List.of(
            new StaffQualification("staff-1", "qual-1", "CERTIFIED", "2027-12-31"), // Aileen: Safeguarding L1
            new StaffQualification("staff-1", "qual-2", "CERTIFIED", "2027-06-30"), // Aileen: First Aid
            new StaffQualification("staff-2", "qual-1", "CERTIFIED", "2027-12-31")  // Gregor: Safeguarding L1 only
    );

    /** Returns qualification IDs held by the given staff member. */
    public Set<String> getQualificationIdsForStaff(String staffId) {
        return records.stream()
                .filter(r -> r.staffId().equals(staffId))
                .map(StaffQualification::qualificationId)
                .collect(Collectors.toSet());
    }

    /** Returns true if the staff member holds ALL of the given qualification IDs. */
    public boolean staffHasAllQualifications(String staffId, Set<String> requiredQualIds) {
        if (requiredQualIds == null || requiredQualIds.isEmpty()) return true;
        return getQualificationIdsForStaff(staffId).containsAll(requiredQualIds);
    }
}
