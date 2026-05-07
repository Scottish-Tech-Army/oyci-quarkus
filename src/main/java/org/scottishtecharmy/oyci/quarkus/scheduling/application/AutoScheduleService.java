package org.scottishtecharmy.oyci.quarkus.scheduling.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.scottishtecharmy.oyci.quarkus.assignment.application.AssignmentScaffoldStore;
import org.scottishtecharmy.oyci.quarkus.assignment.domain.AssignmentStatus;
import org.scottishtecharmy.oyci.quarkus.assignment.domain.EventStaffAssignment;
import org.scottishtecharmy.oyci.quarkus.masterdata.application.StaffQualificationStore;
import org.scottishtecharmy.oyci.quarkus.masterdata.application.StaffRegistry;
import org.scottishtecharmy.oyci.quarkus.masterdata.application.UnavailabilityStore;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.StaffMember;
import org.scottishtecharmy.oyci.quarkus.models.Event;
import org.scottishtecharmy.oyci.quarkus.scheduling.domain.AutoScheduleResult;
import org.scottishtecharmy.oyci.quarkus.services.EventService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class AutoScheduleService {

    private static final String SYSTEM_USER = "auto-scheduler";

    @ConfigProperty(name = "scheduling.required-staff-per-event", defaultValue = "2")
    int requiredStaffPerEvent;

    @Inject EventService eventService;
    @Inject AssignmentScaffoldStore assignmentStore;
    @Inject StaffQualificationStore qualificationStore;
    @Inject EventTypeRequirementStore requirementStore;
    @Inject UnavailabilityStore unavailabilityStore;
    @Inject StaffRegistry staffRegistry;

    /**
     * Runs the auto-scheduler across all upcoming events, filling gaps where
     * PENDING_ACCEPTANCE or CONFIRMED assignments are below the required count.
     */
    public AutoScheduleResult scheduleAll() {
        List<Event> events = eventService.getUpcomingEvents();
        List<EventStaffAssignment> created = new ArrayList<>();
        int skipped = 0;

        for (Event event : events) {
            List<EventStaffAssignment> newAssignments = scheduleEvent(event);
            if (newAssignments.isEmpty()) {
                skipped++;
            }
            created.addAll(newAssignments);
        }

        String message = created.isEmpty()
                ? "All events are already fully staffed or no eligible staff available."
                : created.size() + " assignment(s) created across " + events.size() + " event(s).";

        return new AutoScheduleResult(created.size(), skipped, message, created);
    }

    /**
     * Fills staffing gaps for a single event. Called after a staff member declines.
     */
    public AutoScheduleResult scheduleEvent(String eventId) {
        return eventService.getEventById(eventId)
                .map(event -> {
                    List<EventStaffAssignment> newAssignments = scheduleEvent(event);
                    String message = newAssignments.isEmpty()
                            ? "No eligible replacement staff found."
                            : newAssignments.size() + " replacement assignment(s) created.";
                    return new AutoScheduleResult(newAssignments.size(), 0, message, newAssignments);
                })
                .orElse(new AutoScheduleResult(0, 1, "Event not found.", List.of()));
    }

    private List<EventStaffAssignment> scheduleEvent(Event event) {
        List<EventStaffAssignment> newAssignments = new ArrayList<>();

        List<EventStaffAssignment> existing = assignmentStore.findByEventId(event.eventId());
        long activeCount = existing.stream()
                .filter(a -> a.status() == AssignmentStatus.PENDING_ACCEPTANCE
                          || a.status() == AssignmentStatus.CONFIRMED)
                .count();

        if (activeCount >= requiredStaffPerEvent) {
            return newAssignments; // already fully staffed
        }

        Set<String> requiredQuals = requirementStore.getRequiredQualificationIds(event.eventType());
        String eventDate = extractDatePart(event.eventDate());

        // Exclude staff already active on this event OR who previously declined it
        Set<String> alreadyAssigned = existing.stream()
                .filter(a -> a.status() == AssignmentStatus.PENDING_ACCEPTANCE
                          || a.status() == AssignmentStatus.CONFIRMED
                          || a.status() == AssignmentStatus.CANCELLED)
                .map(EventStaffAssignment::staffId)
                .collect(java.util.stream.Collectors.toSet());

        for (StaffMember staff : staffRegistry.getActive()) {
            if ((long) newAssignments.size() + activeCount >= requiredStaffPerEvent) break;
            if (!staff.active()) continue;
            if (alreadyAssigned.contains(staff.id())) continue;
            if (!qualificationStore.staffHasAllQualifications(staff.id(), requiredQuals)) continue;
            if (isStaffBusyOnDate(staff.id(), eventDate, event.eventId())) continue;
            if (unavailabilityStore.isUnavailableOn(staff.id(), eventDate)) continue;

            EventStaffAssignment assignment = assignmentStore.addWithStatus(
                    event.eventId(), staff.id(), deriveRole(event.eventType()), SYSTEM_USER,
                    AssignmentStatus.PENDING_ACCEPTANCE
            );
            newAssignments.add(assignment);
            alreadyAssigned.add(staff.id());
        }

        return newAssignments;
    }

    /** Returns true if the staff member already has an active assignment on the same calendar date. */
    private boolean isStaffBusyOnDate(String staffId, String eventDate, String excludeEventId) {
        return assignmentStore.findAll().stream()
                .filter(a -> a.staffId().equals(staffId))
                .filter(a -> !a.sessionEventId().equals(excludeEventId))
                .filter(a -> a.status() == AssignmentStatus.PENDING_ACCEPTANCE
                          || a.status() == AssignmentStatus.CONFIRMED)
                .anyMatch(a -> eventService.getEventById(a.sessionEventId())
                        .map(e -> extractDatePart(e.eventDate()).equals(eventDate))
                        .orElse(false));
    }

    /** Extract just the date portion from an ISO date/datetime string. */
    private static String extractDatePart(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return "";
        try {
            return LocalDate.parse(dateStr.substring(0, 10)).toString();
        } catch (Exception e) {
            return dateStr;
        }
    }

    private static String deriveRole(String eventType) {
        if (eventType == null) return "Support";
        return switch (eventType.toUpperCase()) {
            case "MATCHDAY" -> "Matchday Support";
            case "TRAINING" -> "Trainer";
            default -> "Support";
        };
    }
}
