package org.scottishtecharmy.oyci.quarkus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.model.Event;
import org.scottishtecharmy.oyci.quarkus.model.StaffRota;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long eventId;
    private String eventName;
    private String eventTypeName;
    private String description;
    private String eventDate;
    private String startTime;
    private String endTime;
    private String location;
    private Integer maxAttendees;
    private String ownerName;
    private long numOfAttendees;
    private String status;
    private List<StaffAssignmentResponse> staffAssignments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffAssignmentResponse {
        private Long staffId;
        private String staffName;
        private String role;
        private String shiftStart;
        private String shiftEnd;
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static EventResponse from(Event event, long numOfAttendees) {
        return from(event, numOfAttendees, null);
    }

    public static EventResponse from(Event event, long numOfAttendees, List<StaffRota> rotaEntries) {
        String locationName = (event.getLocation() != null) ? event.getLocation().getName() : null;
        String ownerName = (event.getOwner() != null)
                ? event.getOwner().getFirstName() + " " + event.getOwner().getLastName()
                : null;
        String eventTypeName = (event.getEventType() != null) ? event.getEventType().getName() : null;
        String status = (event.getStatus() != null) ? event.getStatus().getValue() : null;

        String eventDate = (event.getStartDatetime() != null) ? event.getStartDatetime().format(DATE_FMT) : null;
        String startTime = (event.getStartDatetime() != null) ? event.getStartDatetime().format(TIME_FMT) : null;
        String endTime = (event.getEndDatetime() != null) ? event.getEndDatetime().format(TIME_FMT) : null;

        List<StaffAssignmentResponse> assignments = null;
        if (rotaEntries != null && !rotaEntries.isEmpty()) {
            assignments = rotaEntries.stream()
                    .map(rota -> {
                        String staffName = (rota.getStaff() != null && rota.getStaff().getUser() != null)
                                ? rota.getStaff().getUser().getFirstName() + " " + rota.getStaff().getUser().getLastName()
                                : null;
                        String shiftStart = (rota.getStartDatetime() != null) ? rota.getStartDatetime().format(TIME_FMT) : null;
                        String shiftEnd = (rota.getEndDatetime() != null) ? rota.getEndDatetime().format(TIME_FMT) : null;
                        return new StaffAssignmentResponse(
                                rota.getStaff() != null ? rota.getStaff().getStaffId() : null,
                                staffName,
                                rota.getRole(),
                                shiftStart,
                                shiftEnd
                        );
                    })
                    .collect(Collectors.toList());
        }

        return new EventResponse(
                event.getEventId(),
                event.getEventName(),
                eventTypeName,
                event.getDescription(),
                eventDate,
                startTime,
                endTime,
                locationName,
                event.getMaxAttendees(),
                ownerName,
                numOfAttendees,
                status,
                assignments
        );
    }
}
