package org.scottishtecharmy.oyci.quarkus.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.scottishtecharmy.oyci.quarkus.entity.EventAssignment;
import org.scottishtecharmy.oyci.quarkus.entity.EventInstance;
import org.scottishtecharmy.oyci.quarkus.entity.User;
import org.scottishtecharmy.oyci.quarkus.enums.Role;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommunicationsService {

    @Inject
    Mailer mailer;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public int notifyStaff(LocalDate fromDate, LocalDate toDate) {
        // Fetch all staff assignments in the date range
        List<EventAssignment> assignments = EventAssignment
                .list("eventInstance.eventDate >= ?1 AND eventInstance.eventDate <= ?2 AND user.role = ?3",
                        fromDate, toDate, Role.STAFF);

        if (assignments.isEmpty()) return 0;

        // Group assignments by staff member
        Map<User, List<EventAssignment>> byStaff = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.user));

        int emailsSent = 0;
        for (Map.Entry<User, List<EventAssignment>> entry : byStaff.entrySet()) {
            User staff = entry.getKey();
            List<EventAssignment> staffAssignments = entry.getValue();

            // Sort by date then time
            staffAssignments.sort((a, b) -> {
                int dateCmp = a.eventInstance.eventDate.compareTo(b.eventInstance.eventDate);
                return dateCmp != 0 ? dateCmp : a.eventInstance.startTime.compareTo(b.eventInstance.startTime);
            });

            String body = buildEmailBody(staff, staffAssignments, fromDate, toDate);
            mailer.send(Mail.withHtml(
                    staff.email,
                    "Your Schedule: " + fromDate.format(DateTimeFormatter.ISO_DATE)
                            + " to " + toDate.format(DateTimeFormatter.ISO_DATE),
                    body
            ));
            emailsSent++;
        }
        return emailsSent;
    }

    private String buildEmailBody(User staff, List<EventAssignment> assignments,
                                   LocalDate from, LocalDate to) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>Hi ").append(staff.name).append(",</h2>");
        sb.append("<p>Here is your schedule from <strong>")
                .append(from.format(DATE_FMT))
                .append("</strong> to <strong>")
                .append(to.format(DATE_FMT))
                .append("</strong>:</p>");
        sb.append("<table border=\"1\" cellpadding=\"8\" cellspacing=\"0\" style=\"border-collapse:collapse;\">");
        sb.append("<tr><th>Date</th><th>Event</th><th>Location</th><th>Start Time</th><th>Duration</th></tr>");

        for (EventAssignment a : assignments) {
            EventInstance ei = a.eventInstance;
            sb.append("<tr>");
            sb.append("<td>").append(ei.eventDate.format(DATE_FMT)).append("</td>");
            sb.append("<td>").append(ei.eventType.name).append("</td>");
            sb.append("<td>").append(ei.location.name).append("</td>");
            sb.append("<td>").append(ei.startTime.format(TIME_FMT)).append("</td>");
            sb.append("<td>").append(ei.eventType.durationMinutes).append(" mins</td>");
            sb.append("</tr>");
        }

        sb.append("</table>");
        sb.append("<p>If you have any questions, please contact your administrator.</p>");
        sb.append("<p>Thank you,<br/>OYCI Team</p>");
        sb.append("</body></html>");
        return sb.toString();
    }
}

