package org.scottishtecharmy.oyci.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.scottishtecharmy.oyci.quarkus.model.StaffRota;
import org.scottishtecharmy.oyci.quarkus.model.User;
import org.scottishtecharmy.oyci.quarkus.model.Leave;
import org.scottishtecharmy.oyci.quarkus.repository.StaffRotaRepository;
import org.scottishtecharmy.oyci.quarkus.repository.UserRepository;
import org.scottishtecharmy.oyci.quarkus.response.EventResponse;
import org.scottishtecharmy.oyci.quarkus.response.StaffResponse;

import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
public class NotificationManager {

    private static final Logger LOG = Logger.getLogger(NotificationManager.class);

    @Inject
    EmailService emailService;

    @Inject
    UserRepository userRepository;

    @Inject
    StaffRotaRepository staffRotaRepository;

    // ── Helper: get email from User ──

    private String getEmailFromUser(User user) {
        if (user != null && user.getContactDetail() != null) {
            return user.getContactDetail().getPrimaryEmail();
        }
        return null;
    }

    private String getFullName(User user) {
        if (user != null) {
            return user.getFirstName() + " " + user.getLastName();
        }
        return "User";
    }

    // ── 1. Notify on event registration — send to the registrant + all admins ──

    public void notifyEventRegistration(String userEmail, String userName, EventResponse event) {
        // Send to the registrant
        emailService.sendEventRegistrationEmail(
            userEmail,
            userName,
            event.getEventName(),
            event.getEventDate() != null ? event.getEventDate() : "",
            event.getLocation() != null ? event.getLocation() : ""
        );

        // Also notify all admins
        notifyAdmins(event, "Event Registration Confirmation");
    }

    // ── 2. Send event reminder to all staff assigned to the event + admins ──

    public void sendEventReminder(EventResponse event) {
        // Notify assigned staff
        notifyAssignedStaff(event, " (Reminder)");

        // Notify admins
        notifyAdmins(event, "Event Reminder");
    }

    // ── 3. Leave request approved/rejected ──

    public void notifyLeaveStatus(StaffResponse staff, Leave leave) {
        String staffName = staff.getFirstName() + " " + staff.getLastName();
        if (staff.getEmail() != null && leave.getStatus() != null) {
            emailService.sendLeaveStatusEmail(staff.getEmail(), staffName, leave.getStatus());
        }
    }

    // ── 4. Alert if staff is on leave ──

    public void alertIfStaffOnLeave(StaffResponse staff, Leave leave) {
        OffsetDateTime now = OffsetDateTime.now();
        String staffName = staff.getFirstName() + " " + staff.getLastName();
        if (leave.getStatus() != null && leave.getStatus().equalsIgnoreCase("approved") &&
            now.isAfter(leave.getStartDatetime()) && now.isBefore(leave.getEndDatetime())) {
            emailService.sendLeaveStatusEmail(staff.getEmail(), staffName, "currently on leave");
        }
    }

    // ── 5. Notify on event creation — send to all admins + assigned staff ──

    public void notifyEventCreated(EventResponse event) {
        // Notify admins
        List<User> admins = userRepository.findAdminUsers();
        for (User admin : admins) {
            String email = getEmailFromUser(admin);
            if (email != null) {
                emailService.sendEventCreatedEmail(
                    email,
                    getFullName(admin),
                    event.getEventName(),
                    event.getEventDate() != null ? event.getEventDate() : "",
                    event.getLocation() != null ? event.getLocation() : ""
                );
            }
        }

        // Notify assigned staff
        notifyAssignedStaff(event, "");
    }

    // ── 5b. Notify on event update — send to all admins + assigned staff ──

    public void notifyEventUpdated(EventResponse event) {
        // Notify admins
        List<User> admins = userRepository.findAdminUsers();
        for (User admin : admins) {
            String email = getEmailFromUser(admin);
            if (email != null) {
                emailService.sendEventCreatedEmail(
                    email,
                    getFullName(admin),
                    event.getEventName() + " (Updated)",
                    event.getEventDate() != null ? event.getEventDate() : "",
                    event.getLocation() != null ? event.getLocation() : ""
                );
            }
        }

        // Notify assigned staff
        notifyAssignedStaff(event, " (Updated)");
    }

    // ── 6. Notify on staff availability update ──

    public void notifyStaffAvailability(String userEmail, String staffName, String availabilityStatus) {
        emailService.sendStaffAvailabilityEmail(
            userEmail,
            staffName,
            availabilityStatus
        );
    }

    // ── 7. Notify on forgot password/unlock ──

    public void notifyForgotPassword(String userEmail, String userName, String unlockLink) {
        emailService.sendForgotPasswordEmail(
            userEmail,
            userName,
            unlockLink
        );
    }

    // ── Private helpers ──

    /**
     * Send event registration email to all admin users
     */
    private void notifyAdmins(EventResponse event, String context) {
        List<User> admins = userRepository.findAdminUsers();
        for (User admin : admins) {
            String email = getEmailFromUser(admin);
            if (email != null) {
                emailService.sendEventRegistrationEmail(
                    email,
                    getFullName(admin),
                    event.getEventName() + " - " + context,
                    event.getEventDate() != null ? event.getEventDate() : "",
                    event.getLocation() != null ? event.getLocation() : ""
                );
                LOG.infof("Notification sent to admin: %s", email);
            }
        }
    }

    /**
     * Send event notification to all staff assigned to the event via staff_rota
     */
    private void notifyAssignedStaff(EventResponse event, String subjectSuffix) {
        if (event.getEventId() == null) return;

        List<StaffRota> rotaEntries = staffRotaRepository.findByEventId(event.getEventId());
        for (StaffRota rota : rotaEntries) {
            if (rota.getStaff() != null && rota.getStaff().getUser() != null) {
                User staffUser = rota.getStaff().getUser();
                String email = getEmailFromUser(staffUser);
                if (email != null) {
                    emailService.sendEventRegistrationEmail(
                        email,
                        getFullName(staffUser),
                        event.getEventName() + subjectSuffix,
                        event.getEventDate() != null ? event.getEventDate() : "",
                        event.getLocation() != null ? event.getLocation() : ""
                    );
                    LOG.infof("Notification sent to assigned staff: %s", email);
                }
            }
        }
    }
}
