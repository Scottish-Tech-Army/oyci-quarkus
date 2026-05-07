package org.scottishtecharmy.oyci.quarkus.assignment.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.scottishtecharmy.oyci.quarkus.assignment.application.AssignmentScaffoldStore;
import org.scottishtecharmy.oyci.quarkus.assignment.domain.AssignmentStatus;
import org.scottishtecharmy.oyci.quarkus.assignment.domain.EligibleStaffCandidate;
import org.scottishtecharmy.oyci.quarkus.assignment.domain.EventEligibility;
import org.scottishtecharmy.oyci.quarkus.masterdata.application.StaffQualificationStore;
import org.scottishtecharmy.oyci.quarkus.masterdata.application.StaffRegistry;
import org.scottishtecharmy.oyci.quarkus.masterdata.application.UnavailabilityStore;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.StaffMember;
import org.scottishtecharmy.oyci.quarkus.models.ErrorResponse;
import org.scottishtecharmy.oyci.quarkus.scheduling.application.EventTypeRequirementStore;
import org.scottishtecharmy.oyci.quarkus.services.EventService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/events/{eventId}/eligibility")
@Produces(MediaType.APPLICATION_JSON)
public class EventEligibilityResource {

    @ConfigProperty(name = "scheduling.required-staff-per-event", defaultValue = "2")
    int requiredStaff;

    private final AssignmentScaffoldStore assignmentStore;

    @Inject EventService eventService;
    @Inject StaffQualificationStore qualificationStore;
    @Inject EventTypeRequirementStore requirementStore;
    @Inject UnavailabilityStore unavailabilityStore;
    @Inject StaffRegistry staffRegistry;

    public EventEligibilityResource(AssignmentScaffoldStore assignmentStore) {
        this.assignmentStore = assignmentStore;
    }

    @GET
    public Response getEligibility(@PathParam("eventId") String eventId) {
        var eventOpt = eventService.getEventById(eventId);
        if (eventOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Event not found: " + eventId))
                    .build();
        }
        var event = eventOpt.get();
        String eventDatePart = extractDatePart(event.eventDate());

        var existingAssignments = assignmentStore.findByEventId(eventId);

        // Staff already actively assigned (or who declined) this specific event
        Set<String> assignedToThisEvent = existingAssignments.stream()
                .filter(a -> a.status() == AssignmentStatus.PENDING_ACCEPTANCE
                          || a.status() == AssignmentStatus.CONFIRMED)
                .map(a -> a.staffId())
                .collect(Collectors.toSet());

        Set<String> declinedThisEvent = existingAssignments.stream()
                .filter(a -> a.status() == AssignmentStatus.CANCELLED)
                .map(a -> a.staffId())
                .collect(Collectors.toSet());

        long activeCount = assignedToThisEvent.size();

        // Staff already booked on another event on the same date
        Set<String> busyOnDate = staffRegistry.getActive().stream()
                .map(StaffMember::id)
                .filter(staffId -> isStaffBusyOnDate(staffId, eventDatePart, eventId))
                .collect(Collectors.toSet());

        Set<String> requiredQuals = requirementStore.getRequiredQualificationIds(event.eventType());

        List<EligibleStaffCandidate> candidates = staffRegistry.getActive().stream()
                .map(staff -> buildCandidate(
                        staff, eventDatePart, requiredQuals,
                        assignedToThisEvent, declinedThisEvent, busyOnDate, activeCount))
                .toList();

        return Response.ok(new EventEligibility(eventId, requiredStaff, (int) activeCount, candidates)).build();
    }

    private EligibleStaffCandidate buildCandidate(
            StaffMember staff,
            String eventDatePart,
            Set<String> requiredQuals,
            Set<String> assignedToThisEvent,
            Set<String> declinedThisEvent,
            Set<String> busyOnDate,
            long activeCount
    ) {
        String name = staff.firstName() + " " + staff.lastName();

        if (assignedToThisEvent.contains(staff.id())) {
            return new EligibleStaffCandidate(staff.id(), name, false, "Already assigned to this event");
        }
        if (declinedThisEvent.contains(staff.id())) {
            return new EligibleStaffCandidate(staff.id(), name, false, "Previously declined this event");
        }
        if (activeCount >= requiredStaff) {
            return new EligibleStaffCandidate(staff.id(), name, false, "Event already at required capacity");
        }
        if (!qualificationStore.staffHasAllQualifications(staff.id(), requiredQuals)) {
            return new EligibleStaffCandidate(staff.id(), name, false, "Missing required qualifications for this event type");
        }
        if (unavailabilityStore.isUnavailableOn(staff.id(), eventDatePart)) {
            return new EligibleStaffCandidate(staff.id(), name, false, "Staff member is marked unavailable on this date");
        }
        if (busyOnDate.contains(staff.id())) {
            return new EligibleStaffCandidate(staff.id(), name, false, "Already assigned to another event on this date");
        }
        return new EligibleStaffCandidate(staff.id(), name, true, "Available and qualified");
    }

    private boolean isStaffBusyOnDate(String staffId, String eventDatePart, String excludeEventId) {
        return assignmentStore.findAll().stream()
                .filter(a -> a.staffId().equals(staffId))
                .filter(a -> !a.sessionEventId().equals(excludeEventId))
                .filter(a -> a.status() == AssignmentStatus.PENDING_ACCEPTANCE
                          || a.status() == AssignmentStatus.CONFIRMED)
                .anyMatch(a -> eventService.getEventById(a.sessionEventId())
                        .map(e -> extractDatePart(e.eventDate()).equals(eventDatePart))
                        .orElse(false));
    }

    private static String extractDatePart(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return "";
        try {
            return LocalDate.parse(dateStr.substring(0, 10)).toString();
        } catch (Exception e) {
            return dateStr;
        }
    }
}
