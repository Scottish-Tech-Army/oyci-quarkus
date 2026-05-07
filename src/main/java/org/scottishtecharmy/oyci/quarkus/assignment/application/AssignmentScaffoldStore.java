package org.scottishtecharmy.oyci.quarkus.assignment.application;

import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.assignment.domain.AssignmentStatus;
import org.scottishtecharmy.oyci.quarkus.assignment.domain.EventStaffAssignment;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AssignmentScaffoldStore {

    private final List<EventStaffAssignment> assignments = new ArrayList<>();

    public AssignmentScaffoldStore() {
        // No seed data — assignments are created via the API or auto-scheduler
    }

    public synchronized List<EventStaffAssignment> findByEventId(String eventId) {
        return assignments.stream()
                .filter(item -> item.sessionEventId().equals(eventId))
                .toList();
    }

    public synchronized List<EventStaffAssignment> findAll() {
        return List.copyOf(assignments);
    }

    public synchronized Optional<EventStaffAssignment> findById(String id) {
        return assignments.stream().filter(a -> a.id().equals(id)).findFirst();
    }

    public synchronized Optional<EventStaffAssignment> updateStatus(String id, AssignmentStatus newStatus) {
        for (int i = 0; i < assignments.size(); i++) {
            if (assignments.get(i).id().equals(id)) {
                EventStaffAssignment existing = assignments.get(i);
                EventStaffAssignment updated = new EventStaffAssignment(
                        existing.id(), existing.sessionEventId(), existing.staffId(),
                        existing.role(), newStatus, existing.assignedAt(),
                        existing.assignedBy(), existing.hoursCommittedSnapshot()
                );
                assignments.set(i, updated);
                return Optional.of(updated);
            }
        }
        return Optional.empty();
    }

    public synchronized EventStaffAssignment add(String eventId, String staffId, String role, String assignedBy) {
        return addWithStatus(eventId, staffId, role, assignedBy, AssignmentStatus.DRAFT);
    }

    public synchronized EventStaffAssignment addWithStatus(String eventId, String staffId, String role,
                                                           String assignedBy, AssignmentStatus status) {
        EventStaffAssignment assignment = new EventStaffAssignment(
                UUID.randomUUID().toString(), eventId, staffId, role, status,
                OffsetDateTime.now().toString(), assignedBy, 2.0
        );
        assignments.add(assignment);
        return assignment;
    }
}

