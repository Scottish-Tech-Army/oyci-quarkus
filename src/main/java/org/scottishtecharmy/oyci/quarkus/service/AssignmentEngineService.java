package org.scottishtecharmy.oyci.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import org.scottishtecharmy.oyci.quarkus.dto.StaffSuitabilityDTO;
import org.scottishtecharmy.oyci.quarkus.entity.*;
import org.scottishtecharmy.oyci.quarkus.enums.Role;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AssignmentEngineService {

    public List<StaffSuitabilityDTO> evaluate(Long eventInstanceId) {
        EventInstance event = EventInstance.findById(eventInstanceId);
        if (event == null) throw new NotFoundException("Event instance not found");

        // Eagerly load event type with required tags
        EventType eventType = event.eventType;
        Set<Long> requiredTagIds = eventType.requiredTags.stream()
                .map(t -> t.id)
                .collect(Collectors.toSet());

        int durationMinutes = eventType.durationMinutes;
        LocalDate eventDate = event.eventDate;
        LocalTime eventStart = event.startTime;
        LocalTime eventEnd = eventStart.plusMinutes(durationMinutes);

        // ISO day of week: Monday=1, Sunday=7
        int dayOfWeek = eventDate.getDayOfWeek().getValue();

        List<User> allStaff = User.list("role", Role.STAFF);
        List<StaffSuitabilityDTO> results = new ArrayList<>();

        for (User staff : allStaff) {
            List<String> warnings = new ArrayList<>();

            // 1. MISSING_QUALIFICATION — staff tags don't cover all required tags
            Set<Long> staffTagIds = staff.tags.stream()
                    .map(t -> t.id)
                    .collect(Collectors.toSet());
            if (!staffTagIds.containsAll(requiredTagIds)) {
                warnings.add("MISSING_QUALIFICATION");
            }

            // 2. OUTSIDE_AVAILABLE_HOURS — event time not within staff availability for that day
            List<UserAvailability> availabilities = UserAvailability.findByUserId(staff.id);
            boolean coveredByAvailability = availabilities.stream()
                    .filter(a -> a.dayOfWeek == dayOfWeek)
                    .anyMatch(a -> !eventStart.isBefore(a.startTime) && !eventEnd.isAfter(a.endTime));
            if (!coveredByAvailability) {
                warnings.add("OUTSIDE_AVAILABLE_HOURS");
            }

            // 3. ON_HOLIDAY — event date falls within a staff holiday range
            List<UserHoliday> holidays = UserHoliday.findByUserId(staff.id);
            boolean onHoliday = holidays.stream()
                    .anyMatch(h -> !eventDate.isBefore(h.startDate) && !eventDate.isAfter(h.endDate));
            if (onHoliday) {
                warnings.add("ON_HOLIDAY");
            }

            // 4. SCHEDULING_CONFLICT — already assigned to an overlapping event this day
            List<EventAssignment> existingAssignments = EventAssignment.findByUserId(staff.id);
            boolean hasConflict = existingAssignments.stream()
                    .filter(a -> !a.eventInstance.id.equals(eventInstanceId))
                    .filter(a -> a.eventInstance.eventDate.equals(eventDate))
                    .anyMatch(a -> {
                        LocalTime otherStart = a.eventInstance.startTime;
                        LocalTime otherEnd = otherStart.plusMinutes(a.eventInstance.eventType.durationMinutes);
                        return eventStart.isBefore(otherEnd) && eventEnd.isAfter(otherStart);
                    });
            if (hasConflict) {
                warnings.add("SCHEDULING_CONFLICT");
            }

            // 5. EXCEEDS_WEEKLY_HOUR_LIMIT — total assigned hours for the week + this event > max
            LocalDate weekStart = eventDate.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = eventDate.with(DayOfWeek.SUNDAY);
            long assignedMinutesThisWeek = existingAssignments.stream()
                    .filter(a -> !a.eventInstance.eventDate.isBefore(weekStart)
                            && !a.eventInstance.eventDate.isAfter(weekEnd))
                    .mapToLong(a -> a.eventInstance.eventType.durationMinutes)
                    .sum();
            long totalMinutes = assignedMinutesThisWeek + durationMinutes;
            if (totalMinutes > (long) staff.maxHoursPerWeek * 60) {
                warnings.add("EXCEEDS_WEEKLY_HOUR_LIMIT");
            }

            String status = warnings.isEmpty() ? "PERFECT_MATCH" : "WARNING";
            results.add(new StaffSuitabilityDTO(staff.id, staff.name, staff.email, status, warnings));
        }

        // Sort: PERFECT_MATCH first, then WARNING
        results.sort((a, b) -> a.status.compareTo(b.status));
        return results;
    }
}

